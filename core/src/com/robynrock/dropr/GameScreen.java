package com.robynrock.dropr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.utils.Array;
import java.util.HashMap;


public class GameScreen extends InputAdapter implements Screen {
    final dropr game;
    final HashMap<String, Sprite> pegSprites = new HashMap<String, Sprite>();

    static final float STEP_TIME = 1f / 60f;
    static final int VELOCITY_ITERATIONS = 6;
    static final int POSITION_ITERATIONS = 2;
    static final int COUNT =39;

    Array<Body> bodiesForDeletion = new Array<Body>();
    Array<Body> newDisc = new Array<Body>();
    Body pegBodies[] = new Body[COUNT];


    float accumulator = 0;

    Body floor, platform, tmpPlatform, disc, tmpDisc, wallL, wallR, winSensor, pitL, pitR;
    Box2DDebugRenderer debugRenderer;
    OrthographicCamera camera;
    TextureAtlas textureAtlas;
    World world;

    Vector2 worldGravity = new Vector2();
    Vector2 centerScreen = new Vector2();

    String TAG = "MouseJoint";
    private Vector3 tmp = new Vector3();
    private Vector2 tmp2 = new Vector2();

    private MouseJointDef mouseJointDef;
    private MouseJoint joint;

    public GameScreen(final dropr gam) {
        this.game = gam;
        worldGravity.set(0, -300);

        //instantiating the box2D world and Renderer.
        Box2D.init();
        world = new World(worldGravity, true);
        debugRenderer = new Box2DDebugRenderer();

        world.setContactListener(new ContactListener() {

            @Override
            public void beginContact(Contact contact) {

                Body fixtureA = contact.getFixtureA().getBody();
                Body fixtureB = contact.getFixtureB().getBody();

                // if the disc and the floor collide.
                if ((fixtureA.getUserData() == disc.getUserData() && fixtureB.getUserData() == floor.getUserData()) || (fixtureA.getUserData() == floor.getUserData() && fixtureB.getUserData() == disc.getUserData())) {
                    newDisc.add(tmpDisc);
                    newDisc.add(tmpPlatform);
                    bodiesForDeletion.add(disc);    // destroy the current disk
                }

                // if contact is detected between disc and WinSensor
                if (fixtureA.getUserData() == winSensor.getUserData() && fixtureB.getUserData() == disc.getUserData()) {

                    Gdx.app.log("WIN_CONDITION", "You won!"); //print win Message to the console;

                    //TODO: Create alert Box to notify user that they have won the game!
                }

                Gdx.app.log("beginContact", "between " + contact.getFixtureA().getBody().getUserData() + " and " + contact.getFixtureB().getBody().getUserData() + ".");
            }

            @Override
            public void endContact(Contact contact) {
                Gdx.app.log("endContact", "between " + contact.getFixtureA().getBody().getUserData() + " and " + contact.getFixtureB().getBody().getUserData() + ".");
            }

            @Override
            public void postSolve(Contact arg0, ContactImpulse arg1) {
                // TODO Auto-generated method stub
            }

            @Override
            public void preSolve(Contact arg0, Manifold arg1) {
                // TODO Auto-generated method stub
            }
        });

        // instantiating the camera and viewport.
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth() / 5, Gdx.graphics.getHeight() / 5);

        // instantiating bodies to create the GameWorlds.
        disc = createCircleBody("disc", 15, .5f, 1f, .001f, camera.viewportWidth / 2, 450, 0, false);

        floor = createRectangleBody("floor", camera.viewportWidth, 1, 0, 0, 0, 1, false);

        platform = createRectangleBody("platform", camera.viewportWidth, 1, 0, 425, 0, 0, false);
        winSensor = createRectangleBody("WinCondition", camera.viewportWidth / 10, 10, camera.viewportWidth / 2, 0, 0, 0, true);

        pitL = createRectangleBody("PitWallLeft", 1, 35, camera.viewportWidth / 2 - (camera.viewportWidth / 10) - 2, 0, 0, 0, false);
        pitR = createRectangleBody("PitWallRight", 1, 35, camera.viewportWidth / 2 + (camera.viewportWidth / 10) + 2, 0, 0, 0, false);

        wallL = createRectangleBody("LeftWall", 1, camera.viewportHeight, 0, 0, 0, 0, false);
        wallR = createRectangleBody("RightWall", 1, camera.viewportHeight, camera.viewportWidth, 0, 0, 0, false);


        // mouse joint
        mouseJointDef = new MouseJointDef();
        mouseJointDef.bodyA = floor;
        mouseJointDef.collideConnected = true;
        mouseJointDef.frequencyHz = 3000;
        mouseJointDef.maxForce = 600;

        centerScreen.set(camera.viewportWidth / 2, camera.viewportHeight / 2);

        //generate the array of Pegs

        generatePegs();


        // create -5 row of pegs
        pegBodies[1].setTransform(camera.viewportWidth / 2 - 125, 150, 0);
        pegBodies[2].setTransform(camera.viewportWidth / 2 - 125, 250, 0);
        pegBodies[3].setTransform(camera.viewportWidth / 2 - 125, 350, 0);

        // create -4 row of pegs
        pegBodies[4].setTransform(camera.viewportWidth / 2 - 100, 100, 0);
        pegBodies[5].setTransform(camera.viewportWidth / 2 - 100, 200, 0);
        pegBodies[6].setTransform(camera.viewportWidth / 2 - 100, 300, 0);
        pegBodies[7].setTransform(camera.viewportWidth / 2 - 100, 400, 0);

        // create -3 row of pegs
        pegBodies[8].setTransform(camera.viewportWidth / 2 - 75, 150, 0);
        pegBodies[9].setTransform(camera.viewportWidth / 2 - 75, 250, 0);
        pegBodies[10].setTransform(camera.viewportWidth / 2 - 75, 350, 0);

        // create -2 row of pegs
        pegBodies[11].setTransform(camera.viewportWidth / 2 - 50, 100, 0);
        pegBodies[12].setTransform(camera.viewportWidth / 2 - 50, 200, 0);
        pegBodies[13].setTransform(camera.viewportWidth / 2 - 50, 300, 0);
        pegBodies[14].setTransform(camera.viewportWidth / 2 - 50, 400, 0);

        //create -1 row of pegs
        pegBodies[15].setTransform(camera.viewportWidth / 2 - 25, 150, 0);
        pegBodies[16].setTransform(camera.viewportWidth / 2 - 25, 250, 0);
        pegBodies[17].setTransform(camera.viewportWidth / 2 - 25, 350, 0);

        //create center Row of Pegs
        pegBodies[18].setTransform(camera.viewportWidth / 2, 100, 0);
        pegBodies[19].setTransform(camera.viewportWidth / 2, 200, 0);
        pegBodies[20].setTransform(camera.viewportWidth / 2, 300, 0);
        pegBodies[21].setTransform(camera.viewportWidth / 2, 400, 0);

        //create +1 row of pegs
        pegBodies[22].setTransform(camera.viewportWidth / 2 + 25, 150, 0);
        pegBodies[23].setTransform(camera.viewportWidth / 2 + 25, 250, 0);
        pegBodies[24].setTransform(camera.viewportWidth / 2 + 25, 350, 0);

        //create +2 Row of Pegs
        pegBodies[25].setTransform(camera.viewportWidth / 2 + 50, 100, 0);
        pegBodies[26].setTransform(camera.viewportWidth / 2 + 50, 200, 0);
        pegBodies[27].setTransform(camera.viewportWidth / 2 + 50, 300, 0);
        pegBodies[28].setTransform(camera.viewportWidth / 2 + 50, 400, 0);

        // create +3 Row of pegs
        pegBodies[29].setTransform(camera.viewportWidth / 2 + 75, 150, 0);
        pegBodies[30].setTransform(camera.viewportWidth / 2 + 75, 250, 0);
        pegBodies[31].setTransform(camera.viewportWidth / 2 + 75, 350, 0);

        // create +4 row of pegs
        pegBodies[32].setTransform(camera.viewportWidth / 2 + 100, 100, 0);
        pegBodies[33].setTransform(camera.viewportWidth / 2 + 100, 200, 0);
        pegBodies[34].setTransform(camera.viewportWidth / 2 + 100, 300, 0);
        pegBodies[35].setTransform(camera.viewportWidth / 2 + 100, 400, 0);

        // create +5 row of pegs
        pegBodies[36].setTransform(camera.viewportWidth / 2 + 125, 150, 0);
        pegBodies[37].setTransform(camera.viewportWidth / 2 + 125, 250, 0);
        pegBodies[38].setTransform(camera.viewportWidth / 2 + 125, 350, 0);

        // setting the input Processor for the mouseJoint
        Gdx.input.setInputProcessor(this);
    }


    /**
     * - - - - - - - - - - - - - - -
     * <p>
     * User created Methods Section
     * <p>
     * * - - - - - - - - - - - - - -
     */

    protected void stepWorld() {
        float delta = Gdx.graphics.getDeltaTime();
        accumulator += Math.min(delta, 0.25f);

        if (accumulator >= STEP_TIME) {
            accumulator -= STEP_TIME;

            world.step(STEP_TIME, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
        }
    }

    protected Body createCircleBody(String name, float radius, float restitution, float friction, float density, float x, float y, float rotation, boolean isStatic) {
        BodyDef bodyDef = new BodyDef();

        if(isStatic) {
            bodyDef.type = BodyDef.BodyType.StaticBody;
        } else {
            bodyDef.type = BodyDef.BodyType.DynamicBody;
        }

        CircleShape shape = new CircleShape();
        shape.setRadius(radius);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.restitution = restitution;
        fixtureDef.friction = friction;
        fixtureDef.shape = shape;
        fixtureDef.density = density;

        Body circle = world.createBody(bodyDef);
        circle.createFixture(fixtureDef);
        circle.setTransform(x, y, rotation);
        circle.setUserData(name);

        return circle;

    }

    protected Body createRectangleBody(String name, float hx, float hy, float x, float y, float z, float friction, boolean sensor) {

        Body rectangle;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(hx, hy);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.friction = friction;
        fixtureDef.shape = shape;

        if(sensor) fixtureDef.isSensor = true;

        rectangle = world.createBody(bodyDef);
        rectangle.createFixture(fixtureDef);
        rectangle.setTransform(x, y, z);
        rectangle.setUserData(name);

        shape.dispose();

        return rectangle;
    }

    private void generatePegs() {

        for (int i = 0; i < pegBodies.length; i++) {
            String name = "peg" + i;
            pegBodies[i] = createCircleBody(name, 5, .5f, .1f, 0, -50, 0, 0, true);
            pegBodies[i].setUserData("peg");
        }
    }


    /**
     * - - - - - - - - - - - -
     * <p>
     * Render section
     * <p>
     * * - - - - - - - - - - - -
     */

    public void render(float delta) {
        stepWorld();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        debugRenderer.render(world, camera.combined);

        if (bodiesForDeletion.size > 0) {
            System.out.println("Bodies ready for deletion!");
            world.destroyBody(bodiesForDeletion.first());
            bodiesForDeletion.clear();
        }

        if (newDisc.size > 1) {
            disc = createCircleBody("disc", 15, .5f, 1f, .001f, camera.viewportWidth / 2, 450, 0, false);
            platform = createRectangleBody("platform", camera.viewportWidth, 1, 0, 425, 0, 0, false);
            newDisc.clear();
        }

    }

    @Override
    public void resize(int width, int height) {


    }

    @Override
    public void show() {
        //TODO: Auto-Generated Method Stub

    }

    @Override
    public void hide() {
        //TODO: Auto-Generated Method Stub

    }

    @Override
    public void pause() {
        //TODO: Auto-Generated Method Stub

    }

    @Override
    public void resume() {
        //TODO: Auto-Generated Method Stub

    }

    @Override
    public void dispose() {
        //TODO: Auto-Generated Method Stub
    }

    /**
     * - - - - - - - - - - - - - - - - -
     * <p>
     * input processor for mouseJoint
     * <p>
     * * - - - - - - - - - - - - - - - - -
     */

    QueryCallback queryCallBack = new QueryCallback() {
        @Override
        public boolean reportFixture(Fixture fixture) {
            if (!fixture.testPoint(tmp.x, tmp.y)) {
                return true;
            }

            mouseJointDef.bodyB = fixture.getBody();
            mouseJointDef.target.set(tmp.x, tmp.y);
            joint = (MouseJoint) world.createJoint(mouseJointDef);
            return false;
        }
    };

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Gdx.app.log(TAG, "touchDown Detected at: " + screenX + ", " + screenY);

        camera.unproject(tmp.set(screenX, screenY, 0));
        world.QueryAABB(queryCallBack, tmp.x, tmp.y, tmp.x, tmp.y);

        worldGravity.set(0, -120);

        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        Gdx.app.log(TAG, "touchDrag Detected at: " + screenX + ", " + screenY);

        if (joint == null)
            return true;

        camera.unproject(tmp.set(screenX, screenY, 0));
        joint.setTarget(tmp2.set(tmp.x, tmp.y));

        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        Gdx.app.log(TAG, "touchUp Detected at: " + screenX + ", " + screenY);
        if (joint == null)
            return false;
        bodiesForDeletion.add(platform);
        world.destroyJoint(joint);
        joint = null;
        return true;
    }
}