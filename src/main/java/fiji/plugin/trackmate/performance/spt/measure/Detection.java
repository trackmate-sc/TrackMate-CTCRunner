/*-
 * #%L
 * Image Analysis Hub support for Life Scientists.
 * %%
 * Copyright (C) 2021 IAH developers.
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the IAH / C2RT / Institut Pasteur nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package fiji.plugin.trackmate.performance.spt.measure;

import java.awt.Color;

/**
 * Detection is the basic detection class. Extends Detection to create more
 * complete Detection.
 * 
 * @author Fabrice de Chaumont
 */

public class Detection
{

	@Override
	public Object clone() throws CloneNotSupportedException
	{

		final Detection clone = ( Detection ) super.clone();

		clone.x = x;
		clone.y = y;
		clone.z = z;
		clone.t = t;
		clone.detectionType = detectionType;
		clone.selected = selected;
		clone.enabled = enabled;
		clone.color = new Color( color.getRed(), color.getGreen(), color.getBlue() );
		clone.originalColor = new Color( originalColor.getRGB() );

		return clone;
	}

	/** x position of detection. */
	protected double x;

	/** y position of detection. */
	protected double y;

	/** z position of detection. */
	protected double z;

	/** t position of detection. */
	protected int t;

	/** default detection type */
	protected int detectionType = DETECTIONTYPE_REAL_DETECTION;

	/** Selected */
	protected boolean selected = false;

	/**
	 * Detection enabled/disable is the internal mechanism to filter track with
	 * TrackProcessor. At the start of TrackProcessor process, the enable track
	 * are all set to true. any TSP can then disable it.
	 */
	protected boolean enabled = true;

	/**
	 * This color is used each time the TrackProcessor start, as it call the
	 * detection.reset() function. This color is loaded when using an XML file.
	 * While saving, the current color of the track ( color propertie ) is used.
	 * So at load it will become the new originalColor.
	 */
	protected Color originalColor = Color.blue;

	public final static int DETECTIONTYPE_REAL_DETECTION = 1;

	public final static int DETECTIONTYPE_VIRTUAL_DETECTION = 2;

	public Detection( final double x, final double y, final double z, final int t )
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.t = t;
		reset();
	}

	public Detection()
	{
		reset();
	}

	public int getT()
	{
		return t;
	}

	public void setT( final int t )
	{
		this.t = t;
	}

	public double getX()
	{
		return x;
	}

	public void setX( final double x )
	{
		this.x = x;
	}

	public double getY()
	{
		return y;
	}

	public void setY( final double y )
	{
		this.y = y;
	}

	public double getZ()
	{
		return z;
	}

	public void setZ( final double z )
	{
		this.z = z;
	}

	@Override
	public String toString()
	{
		return "Detection [x:" + x + " y:" + y + " z:" + z + " t:" + t + "]";
	}

	Color color;

	public int getDetectionType()
	{
		return detectionType;
	}

	public void setDetectionType( final int detectionType )
	{
		this.detectionType = detectionType;
	}

	public boolean isSelected()
	{
		return selected;
	}

	public void setSelected( final boolean selected )
	{
		this.selected = selected;
	}

	public Color getColor()
	{
		return color;
	}

	public void setColor( final Color color )
	{
		this.color = color;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled( final boolean enabled )
	{
		this.enabled = enabled;
	}

	public void reset()
	{
		this.color = originalColor;
		this.setEnabled( true );
	}
}
