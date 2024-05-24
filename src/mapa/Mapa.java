package mapa;

public class Mapa {
    public static final Object lock = new Object();
    private ElementMape map[][];

    public Mapa(){}
    public Mapa(int dimenzija){
        map=new ElementMape[dimenzija][dimenzija];
        for(int i=0;i<dimenzija;i++)
            for(int j=0;j<dimenzija;j++)
                map[i][j]=new ElementMape();
    }

    public ElementMape getElementMape(int x,int y){
        return map[x][y];
    }
}
