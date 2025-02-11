/*
 * Modern UI.
 * Copyright (C) 2019-2021 BloCamLimb. All rights reserved.
 *
 * Modern UI is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Modern UI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Modern UI. If not, see <https://www.gnu.org/licenses/>.
 */

package icyllis.modernui.text;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;

/**
 * @see LayoutCache
 */
@Deprecated
public class LayoutPieces {

    public static final int NO_PAINT_ID = -1;

    private final Key mLookupKey = new Key();

    private final Object2IntMap<FontPaint> mPaintMap = new Object2IntOpenHashMap<>();
    private final Object2ObjectMap<Key, GraphemeMetrics> mMetricsMap = new Object2ObjectOpenHashMap<>();

    public synchronized void insert(int start, int end, GraphemeMetrics piece, boolean dir, FontPaint paint) {
        int paintId = mPaintMap.computeIntIfAbsent(paint, p -> mPaintMap.size());
        if (!mMetricsMap.containsKey(mLookupKey.update(start, end, dir, paintId))) {
            mMetricsMap.put(mLookupKey.copy(), piece);
        }
    }

    public void getOrCreate(@Nonnull char[] textBuf, int start, int end, @Nonnull FontPaint paint,
                            boolean dir, int paintId, @Nonnull BiConsumer<GraphemeMetrics, FontPaint> consumer) {
        final GraphemeMetrics piece;
        synchronized (this) {
            piece = mMetricsMap.get(mLookupKey.update(start, end, dir, paintId));
        }
        if (piece == null) {
            GlyphManager.getInstance().measure(textBuf, start, end, paint, dir, consumer);
        } else {
            consumer.accept(piece, paint);
        }
    }

    public int findPaintId(@Nonnull FontPaint paint) {
        return mPaintMap.getOrDefault(paint, NO_PAINT_ID);
    }

    private static class Key {

        private int start;
        private int end;
        private boolean dir;
        private int paintId;

        public Key() {
        }

        public Key(int start, int end, boolean dir, int paintId) {
            update(start, end, dir, paintId);
        }

        public Key update(int start, int end, boolean dir, int paintId) {
            this.start = start;
            this.end = end;
            this.dir = dir;
            this.paintId = paintId;
            return this;
        }

        @Nonnull
        public Key copy() {
            return new Key(start, end, dir, paintId);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (start != key.start) return false;
            if (end != key.end) return false;
            if (dir != key.dir) return false;
            return paintId == key.paintId;
        }

        @Override
        public int hashCode() {
            int result = start;
            result = 31 * result + end;
            result = 31 * result + (dir ? 1 : 0);
            result = 31 * result + paintId;
            return result;
        }
    }
}
