package com.kraftek.stac.core.model;

public class Extent {
    protected SpatialExtent spatial;
    protected TemporalExtent temporal;

    public SpatialExtent getSpatial() {
        return spatial;
    }

    public void setSpatial(SpatialExtent spatial) {
        this.spatial = spatial;
    }

    public TemporalExtent getTemporal() {
        return temporal;
    }

    public void setTemporal(TemporalExtent temporal) {
        this.temporal = temporal;
    }
}
