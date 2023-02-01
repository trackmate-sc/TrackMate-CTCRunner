/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2021 - 2023 TrackMate developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package fiji.plugin.trackmate.batcher.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.scijava.listeners.Listeners;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.trackmate.io.TmXmlReader;

public class TrackMateReadConfigModel
{
	public interface TrackMateFileListener
	{
		public void trackmateFileChanged();
	}

	/**
	 * Must be a valid TrackMate file or null.
	 */
	private String trackmateFile = null;

	/**
	 * Can be anything, even a non valid TrackMate file.
	 */
	private String proposedFile = null;

	private final transient Listeners.List< TrackMateFileListener > listeners;

	private transient String errorMessage;

	public TrackMateReadConfigModel()
	{
		this.listeners = new Listeners.SynchronizedList<>();
	}

	public Listeners.List< TrackMateFileListener > listeners()
	{
		return listeners;
	}

	/**
	 * Returns a path to the TrackMate file set by
	 * {@link #setProposedFile(String)} or <code>null</code> if this path does
	 * not point to a TrackMate file.
	 * 
	 * @return the path to a TrackMate file or <code>null</code>.
	 */
	public String getTrackMateFile()
	{
		return trackmateFile;
	}

	/**
	 * Returns the proposed file.
	 * 
	 * @return the proposed file.
	 */
	public String getProposedFile()
	{
		return proposedFile;
	}

	/**
	 * If the last call to {@link #setProposedFile(String)} does not point to a
	 * TrackMate file, this method returns an explanatory message.
	 * 
	 * @return the error message when trying to open the TrackMate file set with
	 *         {@link #setProposedFile(String)}.
	 */
	public String getErrorMessage()
	{
		return errorMessage;
	}

	/**
	 * Sets a tentative path to a TrackMate file. If the specified path is not a
	 * TrackMate file, then the {@link #getTrackMateFile()} will return
	 * <code>null</code>, and the {@link #getErrorMessage()} will contains an
	 * explanatory message.
	 * 
	 * @param proposedFile
	 *            a tentative path
	 */
	public void setProposedFile( final String proposedFile )
	{
		if ( this.proposedFile == null && proposedFile == null )
			return;

		if ( ( this.proposedFile == null && proposedFile != null )
				|| ( this.proposedFile != null && proposedFile == null )
				|| ( !this.proposedFile.equals( proposedFile ) ) )
		{
			this.proposedFile = proposedFile;

			try
			{

				if ( proposedFile == null || proposedFile.trim().isEmpty() )
				{
					this.trackmateFile = null;
					return;
				}

				final Path path = Paths.get( proposedFile );
				if ( !Files.exists( path ) )
				{
					errorMessage = "File " + proposedFile + " does not exist.";
					this.trackmateFile = null;
					return;
				}
				if ( !Files.isRegularFile( path ) )
				{
					errorMessage = "File " + proposedFile + " is not a regular file.";
					this.trackmateFile = null;
					return;
				}
				if ( !path.toString().toLowerCase().endsWith( ".xml" ) )
				{
					errorMessage = "File " + proposedFile + " is not a XML file.";
					this.trackmateFile = null;
					return;
				}
				try (BufferedReader br = new BufferedReader( new FileReader( path.toFile() ) ))
				{
					final String line1 = br.readLine();
					if ( !line1.toLowerCase().startsWith( "<?xml" ) )
					{
						errorMessage = "File " + proposedFile + " is not a properly formatted XML file.";
						this.trackmateFile = null;
						return;
					}
					final String line2 = br.readLine();
					if ( !line2.toLowerCase().startsWith( "<trackmate" ) )
					{
						errorMessage = "File " + proposedFile + " is not a TrackMate file.";
						this.trackmateFile = null;
						return;
					}
				}
				catch ( final IOException e1 )
				{
					errorMessage = "Could not open file " + proposedFile + " for reading.";
					this.trackmateFile = null;
					return;
				}

				final TmXmlReader reader = new TmXmlReader( path.toFile() );
				if ( !reader.isReadingOk() )
				{
					errorMessage = "Could not read TrackMate file " + proposedFile
							+ "\nError message is:\n "
							+ reader.getErrorMessage();
					this.trackmateFile = null;
					return;
				}
				this.errorMessage = "";
				this.trackmateFile = proposedFile;
			}
			catch ( final InvalidPathException e )
			{
				errorMessage = "Invalid path: " + e.getMessage();
				this.trackmateFile = null;
				return;
			}
			finally
			{
				notifyListeners();
			}

		}
	}

	/**
	 * Returns the {@link Settings} object stored in the TrackMate file
	 * specified in {@link #getTrackMateFile()}, or <code>null</code> if the
	 * last call to {@link #setProposedFile(String)} did not point to a
	 * TrackMate file.
	 * 
	 * @return a {@link Settings} or <code>null</code>.
	 */
	public Settings getSettings()
	{
		if ( trackmateFile == null )
			return null;

		final TmXmlReader reader = new TmXmlReader( new File( trackmateFile ) );
		return reader.readSettings( null );
	}

	public DisplaySettings getDisplaySettings()
	{
		if ( trackmateFile == null )
			return null;

		final TmXmlReader reader = new TmXmlReader( new File( trackmateFile ) );
		return reader.getDisplaySettings();
	}

	protected void notifyListeners()
	{
		for ( final TrackMateFileListener l : listeners.list )
			l.trackmateFileChanged();
	}
}
