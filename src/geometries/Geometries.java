package geometries;

import primitives.Point;
import primitives.Ray;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Geometries extends Intersectable {

    private List<Intersectable> shapes = null;

    public Geometries() {
        shapes = new LinkedList<>();
    }

    public Geometries(Intersectable... geometries) {
        this();
        add(geometries);
    }

    public void add(Intersectable... geometries) {
        if (shapes == null) {
            throw new IllegalStateException("list not created");
        }
        for (Intersectable i : geometries)
            shapes.add(i);
    }

    @Override
    protected List<GeoPoint> findGeoIntersectionsHelper(Ray ray) {
        List<GeoPoint> lst = null;
        for (Intersectable shape : shapes) {
            List<GeoPoint> shapeList = shape.findGeoIntersections(ray);
            if (shapeList != null) {
                if (lst == null) {
                    lst = new LinkedList<>();
                }
                lst.addAll(shapeList);
            }
        }
        return lst;
    }

}