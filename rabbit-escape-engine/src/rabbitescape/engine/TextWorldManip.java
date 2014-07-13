package rabbitescape.engine;

import static rabbitescape.engine.Direction.*;
import static rabbitescape.engine.util.Util.*;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rabbitescape.engine.ChangeDescription.Change;
import rabbitescape.engine.err.RabbitEscapeException;

public class TextWorldManip
{
    public static class WrongLineLength extends RabbitEscapeException
    {
        private static final long serialVersionUID = 1L;

        public final String lines;
        public final int lineNum;
        public final String line;

        public WrongLineLength( String[] lines, int lineNum )
        {
            this.lines = join( "\n", lines );
            this.lineNum = lineNum + 1; // Human-readable line number
            this.line = lines[lineNum];
        }
    }

    public static class UnknownCharacter extends RabbitEscapeException
    {
        private static final long serialVersionUID = 1L;

        public final String lines;
        public final int lineNum;
        public final char character;

        public UnknownCharacter( String[] lines, int lineNum, int charNum )
        {
            this.lines = join( "\n", lines );
            this.lineNum = lineNum + 1; // Human-readable line number
            this.character = getNth( asChars( lines[lineNum] ), charNum );
        }
    }

    public static World createWorld( String... lines )
    {
        List<Block> blocks = new ArrayList<Block>();
        List<Thing> things = new ArrayList<Thing>();

        Dimension size = processLines( lines, blocks, things );

        return new World( size, blocks, things );
    }

    private static Dimension processLines(
        String[] lines,
        List<Block> blocks,
        List<Thing> things
    )
    {
        if ( lines.length == 0 )
        {
            return new Dimension( 3, 3 );
        }

        int width = lines[0].length();

        int lineNum = 0;
        for ( String line : lines )
        {
            if ( line.length() != width )
            {
                throw new WrongLineLength( lines, lineNum );
            }

            int charNum = 0;
            for ( char c : asChars( line ) )
            {
                switch( c )
                {
                    case ' ':
                    {
                        break;
                    }
                    case '#':
                    {
                        blocks.add( new Block( charNum, lineNum ) );
                        break;
                    }
                    case 'r':
                    {
                        things.add( new Rabbit( charNum, lineNum, RIGHT ) );
                        break;
                    }
                    case 'j':
                    {
                        things.add( new Rabbit( charNum, lineNum, LEFT ) );
                        break;
                    }
                    default:
                    {
                        throw new UnknownCharacter( lines, lineNum, charNum );
                    }
                }
                ++charNum;
            }
            ++lineNum;
        }

        return new Dimension( width, lines.length );
    }

    public static String[] renderWorld( World world )
    {
        char[][] chars = filledChars(
            world.size.height, world.size.width, ' ' );

        for ( Block block : world.blocks )
        {
            chars[ block.y ][ block.x ] = '#';
        }

        for ( Thing thing : world.things )
        {
            chars[ thing.y ][ thing.x ] = charForThing( thing );
        }

        return charsToStrings( chars );
    }

    private static char charForThing( Thing thing )
    {
        if ( thing instanceof Rabbit )
        {
            return ( (Rabbit)thing ).dir == RIGHT ? 'r' : 'j';
        }
        else
        {
            throw new AssertionError(
                "Unknown Thing type: " + thing.getClass() );
        }
    }

    public static String[] renderChangeDescription(
        World world, ChangeDescription desc )
    {
        char[][] chars = filledChars(
            world.size.height, world.size.width, ' ' );

        for ( Change change : desc.changes )
        {
            chars[ change.y ][ change.x ] = charForChange( change );
        }

        return charsToStrings( chars );
    }


    private static char charForChange( Change change )
    {
        switch( change.state )
        {
            case RABBIT_WALKING_LEFT:  return '<';
            case RABBIT_WALKING_RIGHT: return '>';
            default:
                throw new AssertionError(
                    "Unknown Change state: " + change.state.name() );
        }
    }

    private static String[] charsToStrings( char[][] chars )
    {
        String[] ret = new String[chars.length];

        for ( int lineNum = 0; lineNum < chars.length; ++lineNum )
        {
            ret[lineNum] = new String( chars[lineNum] );
        }

        return ret;
    }

    private static char[][] filledChars( int height, int width, char c )
    {
        char[][] ret = new char[height][width];

        for( int i = 0; i < height; ++i )
        {
            Arrays.fill( ret[i], c );
        }

        return ret;
    }
}