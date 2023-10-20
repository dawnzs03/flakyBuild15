package de.blau.android.views.layers;

import android.graphics.Canvas;
import android.view.View;
import androidx.annotation.NonNull;
import de.blau.android.layer.LayerType;
import de.blau.android.resources.TileLayerSource;
import de.blau.android.util.collections.MRUList;
import de.blau.android.views.IMapView;

public class MapTilesOverlayLayer<T> extends MapTilesLayer<T> {

    private static final MRUList<String> lastServers = new MRUList<>(MRU_SIZE);

    private boolean enabled = false;

    /**
     * Construct a tile layer for showing transparent tiles over over tiles/data
     * 
     * @param aView the view we are displaying in
     * @param aTileRenderer the TileRenderer for T
     */
    public MapTilesOverlayLayer(@NonNull final View aView, @NonNull TileRenderer<T> aTileRenderer) {
        super(aView, TileLayerSource.get(aView.getContext(), null, true), null, aTileRenderer);
    }

    @Override
    public boolean isReadyToDraw() {
        TileLayerSource layer = getTileLayerConfiguration();
        if (layer == null) {
            enabled = false;
        } else {
            String id = layer.getId();
            enabled = !(TileLayerSource.LAYER_NOOVERLAY.equals(id) || TileLayerSource.LAYER_NONE.equals(id) || "".equals(id));
        }
        return enabled && super.isReadyToDraw();
    }

    @Override
    public void onDraw(Canvas c, IMapView osmv) {
        if (enabled) {
            super.onDraw(c, osmv);
        }
    }

    @Override
    public LayerType getType() {
        return LayerType.OVERLAYIMAGERY;
    }

    @Override
    MRUList<String> getLastServers() {
        return lastServers;
    }
}
