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
package fiji.plugin.trackmate.helper.spt.measure;

/**
 * Types of distances that can be used between detections DISTANCE_EUCLIDIAN
 * corresponds to a gated Euclidian distance DISTANCE_MATCHING corresponds to a
 * binary penalty: 1 if the Euclidian distance between detections is greater
 * than the gate, 0 otherwise
 * 
 * @version February 3, 2012
 * 
 * @author Nicolas Chenouard
 */
public enum DistanceTypes
{
	DISTANCE_EUCLIDIAN, DISTANCE_MATCHING
}
