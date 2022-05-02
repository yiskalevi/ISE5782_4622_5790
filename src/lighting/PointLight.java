package lighting;

import geometries.Geometries;
import primitives.Color;
import primitives.Double3;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

public class PointLight extends Light  implements LightSource{
    private final Point position;
    private Double3 kC;
    private Double3 kL;
    private Double3 kQ;

    /***
     * constractor with default values for the discount factors
     * @param intensity intensity color
     * @param p the position of the light
     */
    public PointLight(Color intensity, Point p) {
        super(intensity);
        this.position = p;
        kC = 1;
        kL = 0;
        kQ = 0;
    }

    public PointLight setKc(Double3 kc) {
        this.kC = kc;
        return this;
    }
    public PointLight setKc(double kc) {
        this.kC = new Double3(kc);
        return this;
    }

    public PointLight setKl(Double3 kl) {
        this.kL = kl;
        return this;
    }

    public PointLight setKl(double kl) {
        this.kL = new Double3(kl);
        return this;
    }

    public PointLight setKq(Double3 kq) {
        this.kQ = kq;
        return this;
    }
    public PointLight setKq(double kq) {
        this.kQ = new Double3(kq);
        return this;
    }

    @Override
    public Color getIntensity(Point p) {

        double distance = position.distance(p);
        double denominator = kC+kL.scale(distance)+kQ.scale(Math.pow(distance,2));
        return getIntensity().reduce(denominator);
    }

    @Override
    public Vector getL(Point p) {
        //check that we will not get ZERO vector
        if(position.equals(p)){
            return null;
        }
        return p.subtract(position).normalize();
    }
}
