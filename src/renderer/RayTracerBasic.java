package renderer;

import geometries.FlatGeometry;
import geometries.Geometries;
import geometries.Geometry;
import lighting.LightSource;
import primitives.*;
import scene.Scene;
import geometries.Intersectable.GeoPoint;

import java.util.List;


import static primitives.Double3.ZERO;
import static primitives.Util.alignZero;
import static primitives.Util.isZero;

public class RayTracerBasic extends RayTracer{

    private static final int MAX_CALC_COLOR_LEVEL = 10;
    private static final double MIN_CALC_COLOR_K = 0.001;
    private Double3 INITIAL_K = Double3.ONE;

    public RayTracerBasic(Scene scene) {
        super(scene);
    }

    /***
     * return the color of the point that the ray arrive to
     * @param ray the ray that we send
     * @return the color of the point
     */
    @Override
    public Color traceRay(Ray ray) {
        GeoPoint closetPoint = findClosestIntersection(ray);
        return calcColor(closetPoint,ray);
    }

    /***
     * return the color of the point on the geometry
     * by calculate the ambientlight, emmision, intensity of the point
     * @param geopoint point on a geometry
     * @return the color of the point
     */
    private Color calcColor(GeoPoint geopoint, Ray ray) {
        if(geopoint==null){
            return scene.getBackGround();
        }
        return calcColor(geopoint, ray, MAX_CALC_COLOR_LEVEL, INITIAL_K)
                .add(scene.getAmbientLight().getIntensity());
    }
    // recorasifi
    private Color calcColor(GeoPoint gp, Ray ray, int level, Double3 k) {
        Color color = gp.geometry.getEmission();
        color = color.add(calcLocalEffects(gp, ray, k));
        if(1 == level )
            return color;
        return color.add(calcGlobalEffects(gp, ray, level, k));
    }


    private Color calcGlobalEffects(GeoPoint gp, Ray inRay, int level, Double3 k)
    {
        Vector v=inRay.getDir();
        Point point =gp.point;
        Vector n =gp.geometry.getNormal(point);
        double nv=alignZero(n.dotProduct(v));
        if (isZero(nv))
            return gp.geometry.getEmission();

        Color color = Color.BLACK;
        Double3 kr = gp.geometry.getMaterial().kR;
        Double3 kkr = kr.product(k);
        if (!kkr.lowerThan(MIN_CALC_COLOR_K)){
            Ray reflectedRay = constructReflectedRay( point, v,n);
            color = color.add(secondaryRayColor(level, kr, kkr, reflectedRay));
        }
        Double3 kt = gp.geometry.getMaterial().kT;
        Double3 kkt = kt.product(k);
        if (!kkt.lowerThan(MIN_CALC_COLOR_K)){
            Ray refractedRay = constructRefractedRay(point, v, n);
            color = color.add(secondaryRayColor(level, kt, kkt, refractedRay));
        }

        return color;
    }
    private Ray constructRefractedRay(Point pointGeo, Ray inRay, Vector n) {
        return new Ray(pointGeo, inRay.getDir(), n);
    }
    //חישוב קרניים רקורסיבי
    private Color secondaryRayColor(int level, Double3 scalefactor, Double3 kkrt, Ray ray) {
        GeoPoint gp = findClosestIntersection(ray);
        Color color = scene.getBackGround();
        if (gp != null) {
            color = calcColor(gp, ray, level-1, kkrt).scale(scalefactor);
        }
        return color;
    }
    private Ray constructReflectedRay(Point pointGeo, Vector v, Vector n) {
        //r = v - 2.(v.n).n
        double vn = v.dotProduct(n);

        if (vn == 0) {
            return null;
        }

        Vector r = v.subtract(n.scale(2 * vn));
        return new Ray(pointGeo, r, n);
    }
    private Ray constructRefractedRay(Point pointGeo, Vector v, Vector n) {
        return new Ray(pointGeo,v,n);
    }

    private Color calcLocalEffects(GeoPoint gp, Ray ray,Double3 k) {
        Vector v=ray.getDir();
        Point point =gp.point;
        Vector n =gp.geometry.getNormal(point);
        double nv=alignZero(n.dotProduct(v));
        if (isZero(nv))
            return gp.geometry.getEmission();
        Color color = Color.BLACK;

        Material material = gp.geometry.getMaterial();
        for (LightSource lightSource : scene.getLights()) {
            Vector l = lightSource.getL(point);
            double nl = alignZero(n.dotProduct(l));
            if (nl * nv > 0)
                //Double3 ktr=transparency(lightSource,l,n,gp)
                if(unshaded(lightSource,l,n,gp)) {
                    Color iL = lightSource.getIntensity(point);
                    //Color iL = lightSource.getIntensity(point).scale(ktr);
                    color = color.add(
                            iL.scale(calcDiffusive(material, nl)),
                            iL.scale(calcSpecular(material, n, l, nl, v)));
                }
            }
        return color;
    }

    /***
     * Checking for shading between a point and the light source.
     * @param   //geometric point being examined for non-shading between the point and the light source
     * @param
     * @param n
     * @return
     */
    //GeoPoint geopoint,LightSource lightSource , Vector n,double nl, double nv
    //GeoPoint gp, LightSource lightSource , Vector n,double nl, double nv
    private boolean unshaded(LightSource light, Vector l, Vector n, GeoPoint geopoint)
    {
        Vector lightDirection = l.scale(-1); // from point to light source
        double nl=n.dotProduct(lightDirection);

        Point pointGeo = geopoint.point;
        Ray lightRay = new Ray(pointGeo, lightDirection, n);//יש מצב שיש כאן בעיה
        double maxDistance=light.getDistance(pointGeo);
        List<GeoPoint> intersections = scene.getGeometries().findGeoIntersections(lightRay,maxDistance);
        return intersections==null;
  }
    /**
     * calc the diffusive light effect on the specific point
     * @param material the contain the attenuation and shininess factors
     * @param nl dot-product n*l
     * @return double3 of the diffusive factor
     */
    private Double3 calcDiffusive(Material material,double nl) {
        double abs_nl = Math.abs(nl);
        Double3 amount=material.getkD().scale(abs_nl);
        return  amount;
    }

    /**
     * calc the specular light effect on the specific point
     * @param material the contain the attenuation and shininess factors
     * @param n  normal to surface at the point
     * @param l direction from light to point
     * @param nl dot-product n*l
     * @param v  direction from point of view to point
     * @return double3 of the scapular factor
     */
    private Double3 calcSpecular(Material material,Vector n,Vector l,double nl,Vector v) {
        // nl is the dot product among the vector from the specular light to the point and the normal vector of the point
        //nl must not be zero
        if (isZero(nl)) {
            throw new IllegalArgumentException("nl cannot be Zero for scaling the normal vector");
        }
        Vector r = l.subtract(n.scale(2 * nl));
        double vr = alignZero(v.dotProduct(r));
        if (vr >= 0) {
            return ZERO; // view from direction opposite to r vector
        }
        return material.getkS().scale(Math.pow(-1d * vr,material.getShininess()));
    }




    /**
     * find the closest geo point
     * @param ray the ray that create intersection points
     * @return closest geo point
     */
    private GeoPoint findClosestIntersection(Ray ray)
    {

        List<GeoPoint> intersections = scene.getGeometries().findGeoIntersections(ray);
        if (intersections == null)
            return null;
        return ray.findClosestGeoPoint(intersections);
    }


    }

