package mapa;

import figure.Figura;

public class ElementMape {
    private int x;
    private int y;
    private Figura sadrzajPolja;
    private boolean dijamant=false;
    private boolean rupa=false;

    public ElementMape() {}

    public void setX(int x) {this.x = x;}
    public void setY(int y) {this.y = y;}
    public int getY() {return y;}
    public int getX() {return x;}
    public Figura getSadrzajPolja() {return sadrzajPolja;}
    public void setSadrzajPolja(Figura sadrzajPolja) {this.sadrzajPolja = sadrzajPolja;}
    public boolean isDijamant() {return dijamant;}
    public void setDijamant(boolean dijamant) {this.dijamant = dijamant;}
    public boolean isRupa() {return rupa;}
    public void setRupa(boolean rupa) {this.rupa = rupa; }

    public boolean daLiJePoljePrazno(){
        if(sadrzajPolja!=null)
            return false;
        return true;
    }

    public boolean skiniFiguru(){
        if(!daLiJePoljePrazno()) {
            sadrzajPolja=null;
            return true;
        }
        return false;
    }
    public boolean postaviFiguru(Figura figura){
        if(daLiJePoljePrazno()){
            sadrzajPolja=figura;
            return true;
        }
        return false;
    }
}
