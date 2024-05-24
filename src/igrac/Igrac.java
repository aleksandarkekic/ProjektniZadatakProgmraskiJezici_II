package igrac;

import com.sun.tools.javac.Main;
import controllers.MainController;
import figure.Figura;
import figure.SuperBrzaFigura;
import karta.Karta;
import karta.ObicnaKarta;
import karta.SpecijalnaKarta;
import logger.Log;
import mapa.Mapa;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;

public class Igrac extends Thread {
    private String ime;
    private ArrayList<Figura> figureIgraca;
    public int redniBrojURedoslijedu;
    private ObicnaKarta obicnaKarta;
    private SpecijalnaKarta specijalnaKarta;
    public static Random rand = new Random();

    public Igrac() {}

    public Igrac(String ime, ArrayList<Figura> figure) {
        this.ime = ime;
        this.figureIgraca = figure;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }
    public void setFigureIgracal(ArrayList<Figura> figureIgracal) {
        this.figureIgraca = figureIgracal;
    }
    public void setRedniBrojURedoslijedu(int redniBrojURedoslijedu) {this.redniBrojURedoslijedu = redniBrojURedoslijedu;}
    public int getRedniBrojURedoslijedu() {
        return redniBrojURedoslijedu;
    }
    public String getIme() {
        return ime;
    }
    public ArrayList<Figura> getFigureIgracal() {
        return figureIgraca;
    }

    @Override
    public void run() {
        while (true){
            for (int i = 0; i < figureIgraca.size(); i++) {
                while (!figureIgraca.get(i).isZavrsio() && !figureIgraca.get(i).isUpalaUrupu()) {

                    try {
                        while (redniBrojURedoslijedu != MainController.trenutnoIgraIgrac) {
                            synchronized (Mapa.lock) {
                                Mapa.lock.wait();
                            }
                        }
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                    //
                    try {
                        while (MainController.PAUZA) {
                            synchronized (Mapa.lock) {
                                Mapa.lock.wait();
                            }
                        }
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }

                    izvlacenjeKarte();
                    if (obicnaKarta != null) {
                        int brojac = 0;
                        int brojPolja=0;
                        if(figureIgraca.get(i)instanceof SuperBrzaFigura)
                            brojPolja=obicnaKarta.getBroj()*2+figureIgraca.get(i).getBrojDijamata();
                        else
                            brojPolja=obicnaKarta.getBroj()+figureIgraca.get(i).getBrojDijamata();
                        while (brojac != brojPolja) {
                            synchronized (Mapa.lock) {
                                if (!figureIgraca.get(i).isZavrsio()) {
                                    figureIgraca.get(i).kretanje();
                                }
                            }
                                brojac++;
                                try {
                                    sleep(1000);
                                } catch (Exception e) {
                                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                                }

                        }

                        synchronized (Mapa.lock) {
                            MainController.azuriranjeRedoslijeda();
                            Mapa.lock.notifyAll();
                        }
                    } else {
                        synchronized (Mapa.lock) {
                            MainController.postavljanjeRupa();
                        }
                        try {
                            sleep(500);
                        } catch (Exception e) {
                            Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);

                        }

                        synchronized (Mapa.lock) {
                            MainController.akcijaRupe();
                        }
                        try {
                            sleep(500);
                        } catch (Exception e) {
                            Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                        }
                        synchronized (Mapa.lock) {
                            MainController.azuriranjeRedoslijeda();
                            Mapa.lock.notifyAll();
                        }

                    }
                }
            }
            while(!MainController.provjeraKrajaAplikacije()){
                synchronized (Mapa.lock) {
                    MainController.azuriranjeRedoslijeda();
                    Mapa.lock.notifyAll();
                }
                try {
                    while (redniBrojURedoslijedu != MainController.trenutnoIgraIgrac) {
                        synchronized (Mapa.lock) {
                            Mapa.lock.wait();
                        }
                    }
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);                }
                try {
                    sleep(500);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }

            }

            if(MainController.provjeraKrajaAplikacije()) {
                System.out.println("sve figure su zavrsile kretanje!");
                break;
            }


    }
    }
        public void izvlacenjeKarte(){
            int pozicija=rand.nextInt(52);
            if(MainController.spil.get(pozicija) instanceof ObicnaKarta) {
                obicnaKarta = (ObicnaKarta) MainController.spil.get(pozicija);
                MainController.obicnaKarta=obicnaKarta;
                specijalnaKarta=null;
                MainController.specijalnaKarta=null;

            }
            else{
                specijalnaKarta=(SpecijalnaKarta) MainController.spil.get(pozicija);
                MainController.specijalnaKarta=specijalnaKarta;
                obicnaKarta=null;
                MainController.obicnaKarta=null;
            }


        }

    }
