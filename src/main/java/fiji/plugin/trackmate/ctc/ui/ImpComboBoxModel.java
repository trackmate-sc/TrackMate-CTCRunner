package fiji.plugin.trackmate.ctc.ui;

import java.util.Arrays;

import javax.swing.DefaultComboBoxModel;

import ij.ImagePlus;
import ij.WindowManager;

public class ImpComboBoxModel extends DefaultComboBoxModel< ImagePlus >
{

	private static final long serialVersionUID = 1L;

	public ImpComboBoxModel()
	{
		super();
		refresh();
	}

	public void refresh()
	{
		final Object obj = getSelectedItem();
		removeAllElements();

		final int[] idList = WindowManager.getIDList();
		if ( null == idList )
			return;

		Arrays.sort( idList );
		for ( final int id : idList )
		{
			final ImagePlus imp = WindowManager.getImage( id );
			addElement( imp );
		}

		final ImagePlus imp = ( ImagePlus ) obj;
		if ( imp != null && Arrays.binarySearch( idList, imp.getID() ) >= 0 )
			setSelectedItem( imp );
	}
}
