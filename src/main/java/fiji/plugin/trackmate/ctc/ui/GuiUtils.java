package fiji.plugin.trackmate.ctc.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;

public class GuiUtils
{

	public static void changeFont( final Component component, final Font font )
	{
		component.setFont( font );
		if ( component instanceof Container )
			for ( final Component child : ( ( Container ) component ).getComponents() )
				changeFont( child, font );
	}
}
