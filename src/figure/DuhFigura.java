package figure;

import controllers.MainController;
import logger.Log;
import mapa.Mapa;

import java.util.Random;
import java.util.logging.Level;

public class DuhFigura extends Thread {
    private Random rand=new Random();
    private String naziv;

    public DuhFigura(){
        naziv="Duh Figura";
    }

    @Override
    public void run() {
        while (true) {
            synchronized (Mapa.lock) {
                int brojac = 0;
                MainController.skiniDijamante();
                int brojDijamanata = 2 + rand.nextInt(MainController.dimenzijeMatrice - 2);
                int dim = Figura.putanjaKretanja.size() - 1;
                while (brojac != brojDijamanata) {
                    int var = rand.nextInt(dim) + 2;
                    if (var % 2 == 1) {
                        if (!MainController.mapa.getElementMape(Figura.putanjaKretanja.get(var), Figura.putanjaKretanja.get(var - 1)).isDijamant()) {
                            MainController.mapa.getElementMape(Figura.putanjaKretanja.get(var), Figura.putanjaKretanja.get(var - 1)).setDijamant(true);
                            brojac++;
                        }
                    }
                }
            }
            try {
                sleep(5000);
            }catch (Exception e){
                Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
            }
            if(MainController.provjeraKrajaAplikacije()) {
                System.out.println("DUH FIGURA GOTOVA!");
                break;
            }
        }
    }

}
