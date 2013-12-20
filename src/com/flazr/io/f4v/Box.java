/*
 * Flazr <http://flazr.com> Copyright (C) 2009  Peter Thomas.
 *
 * This file is part of Flazr.
 *
 * Flazr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Flazr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Flazr.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.flazr.io.f4v;

import android.util.Log;

import com.flazr.io.BufferReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Box {
    private static final boolean D = false;
    private static final String TAG = Box.class.getSimpleName();

    private final BoxType type;
    private final long fileOffset;
    private List<Box> children;
    private Payload payload;

    public Box(final BufferReader in, final long endPos) {
        final int boxSize = in.readInt();
        final byte[] typeBytes = in.readBytes(4);
        type = BoxType.parse(new String(typeBytes));        
        final long payloadSize;
        if (boxSize == 1) { // extended
            final byte[] extBytes = in.readBytes(8);
            final BigInteger bigLen = new BigInteger(1, extBytes);
            payloadSize = bigLen.intValue() - 16;
        } else if (boxSize == 0) { // decided by parent bound
            payloadSize = endPos - in.position();
        } else {
            payloadSize = boxSize - 8;
        }
        fileOffset = in.position();
        final long childEndPos = fileOffset + payloadSize;
        if (D) {
        	Log.d(TAG, ">> type: " + type + ", payloadSize: " + payloadSize);
        }
        final BoxType[] childBoxes = type.getChildren();
        if(childBoxes == null) {            
            if(type == BoxType.MDAT) {
            	if (D) {
            		Log.d(TAG, "skipping MDAT");
            	}
            	in.position(childEndPos);
                return;
            }          
            payload = type.read(in.read((int) payloadSize));
            if (D) {
            	Log.d(TAG, "<< " + type + " payload: " + payload);
            }
            return;
        }        
        while(in.position() < childEndPos) {
            if(children == null) {
                children = new ArrayList<Box>();
            }
            children.add(new Box(in, childEndPos));
        }
        if (D) {
        	Log.d(TAG, "<< " + type + " children: " + children);
        }
    }

    public BoxType getType() {
        return type;
    }

    public long getFileOffset() {
        return fileOffset;
    }

    public List<Box> getChildren() {
        return children;
    }

    public Payload getPayload() {
        return payload;
    }

    public static void recurse(final Box box, final List<Box> collect, final int level) {
        if(D) {
            final char[] chars = new char[level * 2];
            Arrays.fill(chars, ' ');
            if (D) {
            	Log.d(TAG, "" + String.valueOf(chars) + " recursing " + box.type + ", payload: " + box.payload);
            }
        }        
        if(collect != null && box.getPayload() != null) {
            collect.add(box);
        }
        if(box.getChildren() != null) {
            for(final Box child : box.getChildren()) {
                recurse(child, collect, level + 1);
            }
        }        
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append('[').append(type);
        sb.append(" fileOffset: ").append(fileOffset);
        if(children != null) {
            sb.append(" children: [");
            for(Box box : children) {
                sb.append(box.getType()).append(' ');
            }
            sb.append(']');
        }
        sb.append(" payload: ").append(payload);
        sb.append(']');
        return sb.toString();
    }

}
