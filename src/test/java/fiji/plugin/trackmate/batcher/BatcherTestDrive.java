package fiji.plugin.trackmate.batcher;

import net.imagej.ImageJ;

public class BatcherTestDrive
{
	public static void main( final String[] args )
	{
		final ImageJ ij = new ImageJ();
		ij.launch( args );

		new TrackMateBatcherPlugin().run( null );
	}
}
