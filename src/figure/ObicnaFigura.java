package figure;

import controllers.MainController;

public class ObicnaFigura extends Figura{
    public ObicnaFigura(){}
    public ObicnaFigura(String boja){
       super("OBICNA FIGURA",boja);
    }
    public  void kretanje(){
        int br=0;
        int xx=0,yy=0;
        if(x==0 && y==0) {
            xx = Figura.putanjaKretanja.get(0);
            yy = Figura.putanjaKretanja.get(1);
            while (MainController.mapa.getElementMape(yy, xx).getSadrzajPolja() != null) {
                MainController.predjeniPuteviFigura.get(pozicijaUIstoriji).add(yy);
                MainController.predjeniPuteviFigura.get(pozicijaUIstoriji).add(xx);
                    xx = Figura.putanjaKretanja.get(brojacPozicija);
                    yy = Figura.putanjaKretanja.get(brojacPozicija + 1);
                    brojacPozicija += 2;
                }
            MainController.mapa.getElementMape(yy, xx).postaviFiguru(this);
            if(MainController.mapa.getElementMape(yy, xx).isDijamant()) {
                brojDijamata++;
                MainController.mapa.getElementMape(yy, xx).setDijamant(false);

            }

            MainController.predjeniPuteviFigura.get(pozicijaUIstoriji).add(yy);
            MainController.predjeniPuteviFigura.get(pozicijaUIstoriji).add(xx);
            x=xx;
            y=yy;
        }else {
            MainController.mapa.getElementMape(y, x).skiniFiguru();
            if((brojacPozicija+1)>(Figura.putanjaKretanja.size()-1)){
                zavrsio=true;
            }
            if(!zavrsio) {
                xx = Figura.putanjaKretanja.get(brojacPozicija);
                yy = Figura.putanjaKretanja.get(brojacPozicija + 1);
            }
            brojacPozicija += 2;
            if ((brojacPozicija +1)< Figura.putanjaKretanja.size()){
                while (MainController.mapa.getElementMape(yy, xx).getSadrzajPolja() != null) {
                    if ((brojacPozicija+1 )< Figura.putanjaKretanja.size()) {
                        MainController.predjeniPuteviFigura.get(pozicijaUIstoriji).add(yy);
                        MainController.predjeniPuteviFigura.get(pozicijaUIstoriji).add(xx);
                        xx = Figura.putanjaKretanja.get(brojacPozicija);
                        yy = Figura.putanjaKretanja.get(brojacPozicija + 1);
                        brojacPozicija += 2;
                    }else{
                        zavrsio = true;
                    }
                }
            }else{
                zavrsio=true;
            }
            if(!zavrsio) {
                MainController.mapa.getElementMape(yy, xx).postaviFiguru(this);
                if(MainController.mapa.getElementMape(yy, xx).isDijamant()) {
                    brojDijamata++;
                    MainController.mapa.getElementMape(yy, xx).setDijamant(false);

                }
                MainController.predjeniPuteviFigura.get(pozicijaUIstoriji).add(yy);
                MainController.predjeniPuteviFigura.get(pozicijaUIstoriji).add(xx);
                x = xx;
                y = yy;
            }
        }
    }
}
