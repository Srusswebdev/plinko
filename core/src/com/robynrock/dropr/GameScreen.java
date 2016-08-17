package com.robynrock.dropr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.util.HashMap;


public class GameScreen extends InputAdapter implements Screen {
	final dropr game;
	final HashMap<String, Sprite> pegSprites = new HashMap<String, Sprite>();

	static final float STEP_TIME = 1f / 60f;
	static final int VELOCITY_ITERATIONS = 6;
	static final int POSITION_ITERATIONS = 2;
	static final int COUNT = 20;

	boolean colliding = false;

	Array<Body> bodiesForDeletion = new Array<Body>();
	Array<Body> newDisc = new Array<Body>();
	Body pegBodies[] = new Body[COUNT];


	float accumulator = 0;

	Body tmpFloor, floor, initPlatform, platform, tmpPlatform, disc, tmpDisc, wallL, wallR, winSensor, tmpWinSensor, pitL, pitR;
	Box2DDebugRenderer debugRenderer;
	ExtendViewport viewport;
	OrthographicCamera camera;
	SpriteBatch batch;
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

		worldGravity.set(0, -220);

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

				Body fixtureA = contact.getFixtureA().getBody();
				Body fixtureB = contact.getFixtureB().getBody();

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

		//instantiating the disc, ground, and start platform.
		disc = createCircleBody("Circle", 20, camera.viewportWidth / 2, 450, 0);
		disc.setUserData("disc");

		floor = createGround(tmpFloor);
		floor.setUserData("floor");

		platform = createPlatform("platform", initPlatform);

		winSensor = createWinSensor(tmpWinSensor);
		winSensor.setUserData("winSensor");

		createPitL();
		createPitR();


		centerScreen.set(camera.viewportWidth / 2, camera.viewportHeight / 2);

		//generate the array of Pegs

		generatePegs();

		Body peg1 = pegBodies[0];
		Body peg2 = pegBodies[1];
		Body peg3 = pegBodies[2];
		Body peg4 = pegBodies[3];
		Body peg5 = pegBodies[4];
		Body peg6 = pegBodies[5];
		Body peg7 = pegBodies[6];
		Body peg8 = pegBodies[7];
		Body peg9 = pegBodies[8];
		Body peg10 = pegBodies[9];
		Body peg11 = pegBodies[10];
		Body peg12 = pegBodies[11];
		Body peg13 = pegBodies[12];
		Body peg14 = pegBodies[13];
		Body peg15 = pegBodies[14];
		Body peg16 = pegBodies[15];
		Body peg17 = pegBodies[16];
		Body peg18 = pegBodies[17];


		// create first row of pegs
		peg1.setTransform(40, 100, 0);
		peg2.setTransform(40, 200, 0);
		peg3.setTransform(40, 300, 0);
		peg4.setTransform(40, 400, 0);

		//create second row of pegs
		peg5.setTransform(90, 150, 0);
		peg6.setTransform(90, 250, 0);
		peg7.setTransform(90, 350, 0);

		//create third Row of Pegs
		peg8.setTransform(camera.viewportWidth / 2, 100, 0);
		peg9.setTransform(camera.viewportWidth / 2, 200, 0);
		peg10.setTransform(camera.viewportWidth / 2, 300, 0);
		peg11.setTransform(camera.viewportWidth / 2, 400, 0);

		//create fourth row of pegs
		peg12.setTransform(200, 150, 0);
		peg13.setTransform(200, 250, 0);
		peg14.setTransform(200, 350, 0);

		//create final Row of Pegs
		peg15.setTransform(250, 100, 0);
		peg16.setTransform(250, 200, 0);
		peg17.setTransform(250, 300, 0);
		peg18.setTransform(250, 400, 0);

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

	protected Body createCircleBody(String name, float radius, float x, float y, float rotation) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;

		CircleShape shape = new CircleShape();
		shape.setRadius(radius);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.restitution = .5f;
		fixtureDef.friction = 1;
		fixtureDef.shape = shape;

		Body circle = world.createBody(bodyDef);
		circle.createFixture(fixtureDef);
		circle.setTransform(x, y, rotation);
		circle.setUserData(name);

		return circle;

	}

	protected Body createPegBody(String name, float radius, float x, float y, float rotation) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;

		CircleShape shape = new CircleShape();
		shape.setRadius(radius);

		FixtureDef discDef = new FixtureDef();
		discDef.restitution = 1f;
		discDef.friction = 0;
		discDef.shape = shape;

		Body circle = world.createBody(bodyDef);
		circle.createFixture(discDef);
		circle.setTransform(x, y, rotation);

		return circle;

	}

	protected Body createGround(Body body) {
		if (tmpFloor != null) world.destroyBody(tmpFloor);

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;

		FixtureDef groundDef = new FixtureDef();
		groundDef.friction = 1;

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(camera.viewportWidth, 1);

		groundDef.shape = shape;

		body = world.createBody(bodyDef);
		body.createFixture(groundDef);
		body.setTransform(0, 0, 0);
		body.setUserData(this);

		shape.dispose();

		return body;
	}

	protected Body createWinSensor(Body body) {
		if (tmpFloor != null) world.destroyBody(tmpFloor);

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;

		FixtureDef groundDef = new FixtureDef();
		groundDef.friction = 0;
		groundDef.isSensor = true;

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(camera.viewportWidth / 10, 10);

		groundDef.shape = shape;

		body = world.createBody(bodyDef);
		body.createFixture(groundDef);
		body.setTransform(camera.viewportWidth / 2, 1, 0);
		body.setUserData("ground");

		shape.dispose();

		return body;
	}

	protected Body createPlatform(String name, Body body) {

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.friction = 1;

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(camera.viewportWidth, 1);

		fixtureDef.shape = shape;

		body = world.createBody(bodyDef);
		body.createFixture(fixtureDef);
		body.setTransform(0, 425, 0);
		body.setUserData(name);

		shape.dispose();

		return body;
	}


	protected void createWallR() {

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.friction = 0;

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(1, camera.viewportHeight);

		fixtureDef.shape = shape;

		wallR = world.createBody(bodyDef);
		wallR.createFixture(fixtureDef);
		wallR.setTransform(camera.viewportWidth, 0, 0);

		shape.dispose();
	}

	protected void createPitL() {

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.friction = 0;

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(1, 35);

		fixtureDef.shape = shape;

		pitL = world.createBody(bodyDef);
		pitL.createFixture(fixtureDef);
		pitL.setTransform((camera.viewportWidth / 2 - camera.viewportWidth / 10) - 2, 0, 0);

	}

	protected void createPitR() {

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.friction = 0;

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(1, 35);

		fixtureDef.shape = shape;

		pitR = world.createBody(bodyDef);
		pitR.createFixture(fixtureDef);
		pitR.setTransform((camera.viewportWidth / 2) + (camera.viewportWidth / 10), 0, 0);

	}

	protected void createWallL() {

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.friction = 0;

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(1, camera.viewportHeight);

		fixtureDef.shape = shape;

		wallL = world.createBody(bodyDef);
		wallL.createFixture(fixtureDef);
		wallL.setTransform(0, 0, 0);

		shape.dispose();


	}

	private void generatePegs() {

		for (int i = 0; i < pegBodies.length; i++) {
			String name = "peg" + i;
			pegBodies[i] = createPegBody(name, 5, -50, 0, 0);
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

		// mouse joint
		mouseJointDef = new MouseJointDef();
		mouseJointDef.bodyA = floor;
		mouseJointDef.collideConnected = true;
		mouseJointDef.frequencyHz = 2000;
		mouseJointDef.maxForce = 1000;

		debugRenderer.render(world, camera.combined);

		if (bodiesForDeletion.size > 0) {
			System.out.println("Bodies ready for deletion!");
			world.destroyBody(bodiesForDeletion.first());
			bodiesForDeletion.clear();
		}

		if (newDisc.size > 1) {
			disc = createCircleBody("Disc", 20, camera.viewportWidth / 2, 450, 0);
			platform = createPlatform("platform", initPlatform);
			newDisc.clear();
		}

	}

	@Override
	public void resize(int width, int height) {

		createWallL();
		createWallR();


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
		return true;
	}
}
