package com.robynrock.dropr;


import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.robynrock.dropr.GameScreen;

public class dropr extends Game {
    SpriteBatch batch;


    @Override
    public void create() {
        batch = new SpriteBatch();
        this.setScreen(new GameScreen(this));

    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
    }

}