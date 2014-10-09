package rabbitescape.ui.android;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewConfiguration;

import rabbitescape.engine.Token;
import rabbitescape.engine.World;
import rabbitescape.render.BitmapCache;
import rabbitescape.render.GameLoop;

public class GameSurfaceView extends SurfaceView
    implements
        SurfaceHolder.Callback,
        View.OnClickListener
{
    // Saved state (saved by AndroidGameActivity)
    private final World world;
    private Token.Type chosenAbility;
    private int chosenAbilityIndex;

    // Saved state
    public Game game;

    // Transient state
    private final NumLeftListener numLeftListener;
    private final BitmapCache<AndroidBitmap> bitmapCache;
    private final float displayDensity;
    private final Bundle savedInstanceState;
    private final Scrolling scrolling;

    public GameSurfaceView(
        Context context,
        NumLeftListener numLeftListener,
        BitmapCache<AndroidBitmap> bitmapCache,
        World world,
        float displayDensity,
        Bundle savedInstanceState
    )
    {
        super( context );
        this.numLeftListener = numLeftListener;
        this.bitmapCache = bitmapCache;
        this.world = world;
        this.displayDensity = displayDensity;
        this.savedInstanceState = savedInstanceState;

        game = null;
        scrolling = new Scrolling( this, ViewConfiguration.get( context ).getScaledTouchSlop() );
        chosenAbility = null;
        chosenAbilityIndex = -1;

        getHolder().addCallback( this );
    }

    @Override
    public void surfaceCreated( SurfaceHolder surfaceHolder )
    {
        game = new Game( surfaceHolder, bitmapCache, world, displayDensity, savedInstanceState );
        game.start();

        setOnClickListener( this );
    }

    @Override
    public void surfaceChanged( SurfaceHolder surfaceHolder, int i, int i2, int i3 )
    {
    }

    @Override
    public void surfaceDestroyed( SurfaceHolder surfaceHolder )
    {
        if ( game != null )
        {
            game.stop();
        }
        game = null;
    }

    public boolean togglePaused()
    {
        if ( game != null )
        {
            AndroidGameLoop gameLoop = game.gameLoop;
            gameLoop.paused = !gameLoop.paused;
            return gameLoop.paused;
        }
        else
        {
            return false;
        }
    }

    @Override
    public void onClick( View view )
    {
        if ( game == null || chosenAbility == null )
        {
            return;
        }

        int numLeft = game.gameLoop.addToken( chosenAbility, scrolling.curX, scrolling.curY );

        numLeftListener.numLeft( chosenAbilityIndex, numLeft );
    }

    @Override
    public boolean onTouchEvent( MotionEvent event )
    {
        return scrolling.onTouchEvent( event );
    }

    public void chooseAbility( Token.Type ability, int abilityIndex )
    {
        chosenAbility = ability;
        chosenAbilityIndex = abilityIndex;
    }

    public void onSaveInstanceState( Bundle outState )
    {
        if ( game != null )
        {
            game.gameLoop.onSaveInstanceState( outState );
        }
    }
}