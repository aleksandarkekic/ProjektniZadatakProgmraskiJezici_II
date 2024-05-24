package figure;

import controllers.MainController;
import karta.Karta;
import mapa.Mapa;

import java.util.ArrayList;

public abstract class Figura{
    public static ArrayList<Integer> putanjaKretanja=new ArrayList<Integer>();
    protected   int brojPoljaPomjeranje=2;
    protected String nazivFigure;
    protected String boja;
    protected int x=0;//postaviti pocetne pozicije prilikom kreiranju na osnovu velicine matrice
    protected int y=0;//postaviti pocetne pozicije prilikom kreiranju na osnovu velicine matrice
    protected boolean zavrsio=false;
    protected int brojacPozicija=2;
    protected boolean prviPotez=true;
    protected int pozicijaUIstoriji;
    protected boolean upalaUrupu=false;
    protected int brojDijamata=0;



    public Figura(){}
    public Figura(String nazivFigure,String boja){
        this.nazivFigure=nazivFigure;
        this.boja=boja;
    }

    public abstract void kretanje();

    public int getX() {return x;}
    public int getY() {return y;}
    public String getBoja() {return boja;}
    public String getNazivFigure() {return nazivFigure;}
    public void setX(int x) {this.x = x;}
    public void setY(int y) {this.y = y;}
    public void setBoja(String boja) {this.boja = boja;}
    public void setNazivFigure(String nazivFigure) {this.nazivFigure = nazivFigure;}
    public boolean isZavrsio() {return zavrsio;}
    public void setZavrsio(boolean zavrsio) {this.zavrsio = zavrsio;}
    public boolean isPrviPotez() {return prviPotez;}
    public void setPrviPotez(boolean prviPotez) {this.prviPotez = prviPotez;}
    public void setBrojacPozicija(int brojacPozicija) {this.brojacPozicija = brojacPozicija;}
    public int getBrojacPozicija() {return brojacPozicija;}
    public void setPozicijaUIstoriji(int pozicijaUIstoriji) {this.pozicijaUIstoriji = pozicijaUIstoriji;}
    public int getPozicijaUIstoriji() {return pozicijaUIstoriji;}//to ce biti redni broj u skupu svih figura

    public boolean isUpalaUrupu() {return upalaUrupu;}
    public void setUpalaUrupu(boolean upalaUrupu) {this.upalaUrupu = upalaUrupu;}
      public int getBrojDijamata() {return brojDijamata;}
}
