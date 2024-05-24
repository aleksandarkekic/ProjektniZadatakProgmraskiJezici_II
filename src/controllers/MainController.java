package controllers;

import figure.*;
import icons.Slika;
import igrac.Igrac;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import karta.Karta;
import karta.ObicnaKarta;
import karta.SpecijalnaKarta;
import logger.Log;
import mapa.Mapa;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

public class MainController implements Initializable {
    private static Random rand = new Random();
    public static int brojIgraca = 0;
    public static int dimenzijeMatrice = 0;
    public static final String PUTANJA_KRETANJA = "src/config/podesavanjePutanjeKretanja";
    public static final String PUTANJA_KRETANJA_2 = "src/config/podesavanjePutanjeKretanja2";
    public static final String PUTANJA_REZULTATI = "src/rezultati";
    public static Mapa mapa;
    public static ArrayList<Igrac> igraci = new ArrayList<Igrac>();
    public static ArrayList<Karta> spil = new ArrayList<Karta>();
    public static ArrayList<Integer> redoslijed = new ArrayList<Integer>();
    public static ArrayList<Figura> sveFigure = new ArrayList<Figura>();
    public static HashMap<Integer, String> mapiraneBoje = new HashMap<Integer, String>();
    public static ObicnaKarta obicnaKarta=null;
    public static SpecijalnaKarta specijalnaKarta=null;
    public static int trenutnoIgraIgrac = 0;
    public static String porukaNaKonzoli=null;
    public static int brojRupa=5;
    public PrikazKretanjaController prikazKretanjaController;
    public static HashMap<Integer, ArrayList<Integer>> predjeniPuteviFigura = new HashMap<Integer, ArrayList<Integer>>();
    public static int brojacIstorije=0;
    private long pocetno_vrijeme=0;
    public static boolean PAUZA=false;

    @FXML
    Label myLabel1;
    @FXML
    Label opisKarte;
    @FXML
    Label igrac1;
    @FXML
    Label igrac2;
    @FXML
    Label igrac3;
    @FXML
    Label igrac4;
    @FXML
    GridPane myGridPane;
    @FXML
    Button myButton1;
    @FXML
    ImageView kartaSlika;
    @FXML
    Label vrijemeLabel;
    @FXML
    Label informacije;
    @FXML
    ImageView figurica1,figurica2,figurica3,figurica4,figurica5,figurica6,figurica7,figurica8,
            figurica9,figurica10,figurica11,figurica12,figurica13,figurica14,figurica15,figurica16;
    @FXML
    Button figura1,figura2,figura3,figura4,figura5,figura6,figura7,figura8,figura9,figura10,
    figura11,figura12,figura13,figura14,figura15,figura16;
    @FXML
    Label vremenskoTrajanje;

    private void matodaLabel1() {
        File file=new File(PUTANJA_REZULTATI);
        myLabel1.setText("Trenutni broj odigranih " + "\n" +
                "igara je: " + file.list().length);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mapa = new Mapa(dimenzijeMatrice);
        matodaLabel1();
        podesavanjePutanjeKretanja();
        kreiranjeSpila();
        podesavanjeRedoslijeda(brojIgraca);
        mapiranjeBoje();
        metodaIgraci();
        kreiranjeIgraca();
        podesavanjeIkonicaFigura();
        kreiranjeMapePredjenihPutevaFigura();


        for (int i = 0; i < dimenzijeMatrice; i++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPercentWidth(-1);//podesavanje koliko procenata ce zauzimati
            myGridPane.getColumnConstraints().add(columnConstraints);
        }
        for (int j = 0; j < dimenzijeMatrice; j++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPercentHeight(-1);
            myGridPane.getRowConstraints().add(rowConstraints);
        }
        myGridPane.setGridLinesVisible(true);
         int brojac=1;
        for (int i = 0; i < dimenzijeMatrice; i++) {
            for (int j = 0; j < dimenzijeMatrice; j++) {
                try {
                    ImageView myImageView = new ImageView();
                    myImageView.setFitWidth(50);
                    myImageView.setFitHeight(50);
                    myGridPane.add(new TilePane(myImageView), i, j);
                    Label broj=new Label(String.valueOf(brojac++));
                    broj.setFont(Font.font(18.0));
                    myGridPane.add(broj,j,i);
                    GridPane.setHalignment(broj, HPos.CENTER);
                    GridPane.setValignment(broj, VPos.CENTER);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }
        }
    }//////

    public void pokreniSimulaciju(ActionEvent actionEvent) {
         pocetno_vrijeme = System.currentTimeMillis();
        myButton1.setDisable(true);
        Runnable mapDrawer = () -> {
            while (true) {
               ///
                try {
                    while (MainController.PAUZA) {
                        synchronized (Mapa.lock) {
                            Mapa.lock.wait();
                        }
                    }
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
                ///

                List<Node> list = (List) this.myGridPane.getChildren().stream().filter((node) -> {
                    return node instanceof ImageView;
                }).collect(Collectors.toList());
                Platform.runLater(() -> {
                    this.myGridPane.getChildren().removeAll(list);
                });

                synchronized (Mapa.lock) {
                    //  vrijemeLabel.setText("Vrijeme trajanje igre: "+(System.currentTimeMillis()-pocetno_vrijeme)/100.0);
                    for (int i = 0; i < dimenzijeMatrice; i++) {
                        for (int j = 0; j < dimenzijeMatrice; j++) {
                            final int koordinataX = i;
                            final int koordinataY = j;
                            FileInputStream imageStream = null;
                            /*
                            if(mapa.getElementMape(i,j).isDijamant()){
                                try {
                                    imageStream = new FileInputStream(Slika.SLIKA_DIJAMANT);
                                    Image image = new Image(imageStream);
                                    ImageView myImageView = new ImageView();
                                    myImageView.setImage(image);
                                    myImageView.setFitWidth(myGridPane.getPrefWidth() / (double) 8);
                                    myImageView.setFitHeight(myGridPane.getPrefWidth() / (double) 8);
                                    Platform.runLater(() -> {
                                        this.myGridPane.add(myImageView, koordinataX, koordinataY);
                                    });

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                             */
                            if (!mapa.getElementMape(i, j).daLiJePoljePrazno()) {
                                try {
                                    if (mapa.getElementMape(i, j).getSadrzajPolja() instanceof ObicnaFigura) {
                                        if ("ZELENA".equals(mapa.getElementMape(i, j).getSadrzajPolja().getBoja()))
                                            imageStream = new FileInputStream(Slika.SLIKA_OBICNA_ZELENA);
                                        else if ("CRVENA".equals(mapa.getElementMape(i, j).getSadrzajPolja().getBoja()))
                                            imageStream = new FileInputStream(Slika.SLIKA_OBICNA_CRVENA);
                                        else if ("PLAVA".equals(mapa.getElementMape(i, j).getSadrzajPolja().getBoja()))
                                            imageStream = new FileInputStream(Slika.SLIKA_OBICNA_PLAVA);
                                        else if ("ZUTA".equals(mapa.getElementMape(i, j).getSadrzajPolja().getBoja()))
                                            imageStream = new FileInputStream(Slika.SLIKA_OBICNA_ZUTA);
                                    } else if (mapa.getElementMape(i, j).getSadrzajPolja() instanceof LebdecaFigura) {
                                        if ("ZELENA".equals(mapa.getElementMape(i, j).getSadrzajPolja().getBoja()))
                                            imageStream = new FileInputStream(Slika.SLIKA_LETECA_ZELENA);
                                        else if ("CRVENA".equals(mapa.getElementMape(i, j).getSadrzajPolja().getBoja()))
                                            imageStream = new FileInputStream(Slika.SLIKA_LETECA_CRVENA);
                                        else if ("PLAVA".equals(mapa.getElementMape(i, j).getSadrzajPolja().getBoja()))
                                            imageStream = new FileInputStream(Slika.SLIKA_LETECA_PLAVA);
                                        else if ("ZUTA".equals(mapa.getElementMape(i, j).getSadrzajPolja().getBoja()))
                                            imageStream = new FileInputStream(Slika.SLIKA_LETECA_ZUTA);
                                    } else if (mapa.getElementMape(i, j).getSadrzajPolja() instanceof SuperBrzaFigura) {
                                        if ("ZELENA".equals(mapa.getElementMape(i, j).getSadrzajPolja().getBoja()))
                                            imageStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZELENA);
                                        else if ("CRVENA".equals(mapa.getElementMape(i, j).getSadrzajPolja().getBoja()))
                                            imageStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_CRVENA);
                                        else if ("PLAVA".equals(mapa.getElementMape(i, j).getSadrzajPolja().getBoja()))
                                            imageStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_PLAVA);
                                        else if ("ZUTA".equals(mapa.getElementMape(i, j).getSadrzajPolja().getBoja()))
                                            imageStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZUTA);
                                    }
                                    Image image = new Image(imageStream);
                                    ImageView myImageView = new ImageView();
                                    myImageView.setImage(image);
                                    myImageView.setFitWidth(myGridPane.getPrefWidth() / (double) 10);
                                    myImageView.setFitHeight(myGridPane.getPrefWidth() / (double) 10);
                                    Platform.runLater(() -> {
                                        this.myGridPane.add(myImageView, koordinataX, koordinataY);
                                    });
                                } catch (IOException e) {
                                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                                }

                            }

                            if (mapa.getElementMape(i, j).isRupa()) {
                                try {
                                    imageStream = new FileInputStream(Slika.SLIKA_RUPA);
                                    Image image = new Image(imageStream);
                                    ImageView myImageView = new ImageView();
                                    myImageView.setImage(image);
                                    myImageView.setFitWidth(myGridPane.getPrefWidth() / (double) 8);
                                    myImageView.setFitHeight(myGridPane.getPrefWidth() / (double) 8);
                                    Platform.runLater(() -> {
                                        this.myGridPane.add(myImageView, koordinataX, koordinataY);
                                    });

                                } catch (Exception e) {
                                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                                }
                            }
                        }

                    }
                    resertImageView();
                    long krajnje_vrijeme=(System.currentTimeMillis()-pocetno_vrijeme)/1000;
                    Platform.runLater(() -> {
                       vremenskoTrajanje.setText("Trajanje aplikacije je: "+krajnje_vrijeme+" s.");
                    });
                }
                ;
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }

                if(provjeraKrajaAplikacije()){
                    kreirajFajlove();
                    Platform.exit();
                    System.exit(0);
                }
            }
        };


        Thread thread = new Thread(mapDrawer);
        thread.start();
       for(int i=0;i<brojIgraca;i++)
           igraci.get(i).start();
       DuhFigura duhFigura=new DuhFigura();
       duhFigura.start();

    }

    public void  resertImageView() {
        kartaSlika.imageProperty().set(null);
        try {
            if (obicnaKarta !=null) {
                if (obicnaKarta.getBroj() == 1){
                    Platform.runLater(() -> {
                        try {
                            kartaSlika.setImage(new Image(new FileInputStream(Slika.SLIKA_ONE)));
                            opisKarte.setText("Obicna i Lebdeca" + "\n" +
                                    " figura prelaze 1 mjesto," + "\n" +
                                    " dok Super Brza prelazi 2 mjesta");
                            informacije.setText("Trenutno je na potezu "+(trenutnoIgraIgrac+1)+". igrac.");
                        }catch (Exception e){
                            Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                        }
                    });

                }
                else if (obicnaKarta.getBroj() == 2) {
                    Platform.runLater(() -> {
                        try {
                            kartaSlika.setImage(new Image(new FileInputStream(Slika.SLIKA_TWO)));
                            opisKarte.setText("Obicna i Lebdeca" +"\n"+
                                    " figura prelaze 2 mjesto," +"\n"+
                                    " dok Super Brza prelazi 4 mjesta");
                            informacije.setText("Trenutno je na potezu "+(trenutnoIgraIgrac+1)+". igrac.");
                        }
                        catch (Exception e){
                            Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                        }

                    });


                }
               else if (obicnaKarta.getBroj() == 3) {
                   Platform.runLater(() -> {
                        try {
                            kartaSlika.setImage(new Image(new FileInputStream(Slika.SLIKA_THREE)));
                            opisKarte.setText("Obicna i Lebdeca" +"\n"+
                                    " figura prelaze 3 mjesto," +"\n"+
                                    " dok Super Brza prelazi 6 mjesta");
                            informacije.setText("Trenutno je na potezu "+(trenutnoIgraIgrac+1)+". igrac.");
                        }
                        catch (Exception e){
                            Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                        }

                    });

                }
                else if (obicnaKarta.getBroj() == 4) {
                    Platform.runLater(() -> {
                        try {
                            kartaSlika.setImage(new Image(new FileInputStream(Slika.SLIKA_FOUR)));
                            opisKarte.setText("Obicna i Lebdeca" + "\n" +
                                    " figura prelaze 4 mjesto," + "\n" +
                                    " dok Super Brza prelazi 8 mjesta");
                            informacije.setText("Trenutno je na potezu "+(trenutnoIgraIgrac+1)+". igrac.");
                        }catch (Exception e){
                            Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                        }
                    });

                }
            } else{
                Platform.runLater(() -> {
                    try {
                        kartaSlika.setImage(new Image(new FileInputStream(Slika.SLIKA_SPEC)));
                        opisKarte.setText("Izucena je specijalna karta "+
                                "\n"+"postavlja se "+brojRupa+" rupa!");
                        informacije.setText("Trenutno je na potezu "+(trenutnoIgraIgrac+1)+". igrac.");
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                });
            }

        } catch (Exception e) {
            Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
        }

    }

    //////
    private void podesavanjePutanjeKretanja() {
        if (dimenzijeMatrice == 7 || dimenzijeMatrice==8) {
            try {
                Path path = Paths.get(PUTANJA_KRETANJA);
                List<String> linije = Files.readAllLines(path);
                for (int i = 0; i < linije.size(); i++) {
                    String parts[] = linije.get(i).split("#");
                    for (String part : parts) {
                        String parts2[] = part.split(" ");
                        for (String part2 : parts2) {
                            Figura.putanjaKretanja.add(Integer.parseInt(part2));
                        }
                    }
                }

            } catch (Exception e) {
                Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
            }
        } else  if (dimenzijeMatrice == 9 || dimenzijeMatrice==10) {
            try {
                Path path = Paths.get(PUTANJA_KRETANJA_2);
                List<String> linije = Files.readAllLines(path);
                for (int i = 0; i < linije.size(); i++) {
                    String parts[] = linije.get(i).split("#");
                    for (String part : parts) {
                        String parts2[] = part.split(" ");
                        for (String part2 : parts2) {
                            Figura.putanjaKretanja.add(Integer.parseInt(part2));
                        }
                    }
                }

            } catch (Exception e) {
                Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
            }
        }
    }

    public static void azuriranjeRedoslijeda() {
        trenutnoIgraIgrac = (trenutnoIgraIgrac + 1) % (brojIgraca);
    }

    private void podesavanjeRedoslijeda(int brojIgracaProm) {
        int brojac = 0;
        while (brojac != brojIgracaProm) {
            int broj = rand.nextInt(brojIgracaProm);
            if (!redoslijed.contains(broj)) {
                redoslijed.add(broj);
                brojac++;
            }
        }
    }

    private void mapiranjeBoje() {
        mapiraneBoje.put(0, Boje.CRVENA.toString());
        mapiraneBoje.put(1, Boje.PLAVA.toString());
        mapiraneBoje.put(2, Boje.ZELENA.toString());
        mapiraneBoje.put(3, Boje.ZUTA.toString());
    }

    private void metodaIgraci() {
        if (brojIgraca == 2) {
            igrac1.setText("Igrac1");
            if ("CRVENA".equals(mapiraneBoje.get(redoslijed.get(0))))
                igrac1.setTextFill(Color.RED);
            else if ("PLAVA".equals(mapiraneBoje.get(redoslijed.get(0))))
                igrac1.setTextFill(Color.BLUE);
            else if ("ZELENA".equals(mapiraneBoje.get(redoslijed.get(0))))
                igrac1.setTextFill(Color.GREEN);
            else if ("ZUTA".equals(mapiraneBoje.get(redoslijed.get(0))))
                igrac1.setTextFill(Color.YELLOW);

            igrac2.setText("Igrac2");
            if ("CRVENA".equals(mapiraneBoje.get(redoslijed.get(1))))
                igrac2.setTextFill(Color.RED);
            else if ("PLAVA".equals(mapiraneBoje.get(redoslijed.get(1))))
                igrac2.setTextFill(Color.BLUE);
            else if ("ZELENA".equals(mapiraneBoje.get(redoslijed.get(1))))
                igrac2.setTextFill(Color.GREEN);
            else if ("ZUTA".equals(mapiraneBoje.get(redoslijed.get(1))))
                igrac2.setTextFill(Color.YELLOW);

        } else if (brojIgraca == 3) {
            igrac1.setText("Igrac1");
            if ("CRVENA".equals(mapiraneBoje.get(redoslijed.get(0))))
                igrac1.setTextFill(Color.RED);
            else if ("PLAVA".equals(mapiraneBoje.get(redoslijed.get(0))))
                igrac1.setTextFill(Color.BLUE);
            else if ("ZELENA".equals(mapiraneBoje.get(redoslijed.get(0))))
                igrac1.setTextFill(Color.GREEN);
            else if ("ZUTA".equals(mapiraneBoje.get(redoslijed.get(0))))
                igrac1.setTextFill(Color.YELLOW);

            igrac2.setText("Igrac2");
            if ("CRVENA".equals(mapiraneBoje.get(redoslijed.get(1))))
                igrac2.setTextFill(Color.RED);
            else if ("PLAVA".equals(mapiraneBoje.get(redoslijed.get(1))))
                igrac2.setTextFill(Color.BLUE);
            else if ("ZELENA".equals(mapiraneBoje.get(redoslijed.get(1))))
                igrac2.setTextFill(Color.GREEN);
            else if ("ZUTA".equals(mapiraneBoje.get(redoslijed.get(1))))
                igrac2.setTextFill(Color.YELLOW);

            igrac3.setText("Igrac3");
            if ("CRVENA".equals(mapiraneBoje.get(redoslijed.get(2))))
                igrac3.setTextFill(Color.RED);
            else if ("PLAVA".equals(mapiraneBoje.get(redoslijed.get(2))))
                igrac3.setTextFill(Color.BLUE);
            else if ("ZELENA".equals(mapiraneBoje.get(redoslijed.get(2))))
                igrac3.setTextFill(Color.GREEN);
            else if ("ZUTA".equals(mapiraneBoje.get(redoslijed.get(2))))
                igrac3.setTextFill(Color.YELLOW);
        } else if (brojIgraca == 4) {
            igrac1.setText("Igrac1");
            if ("CRVENA".equals(mapiraneBoje.get(redoslijed.get(0))))
                igrac1.setTextFill(Color.RED);
            else if ("PLAVA".equals(mapiraneBoje.get(redoslijed.get(0))))
                igrac1.setTextFill(Color.BLUE);
            else if ("ZELENA".equals(mapiraneBoje.get(redoslijed.get(0))))
                igrac1.setTextFill(Color.GREEN);
            else if ("ZUTA".equals(mapiraneBoje.get(redoslijed.get(0))))
                igrac1.setTextFill(Color.YELLOW);

            igrac2.setText("Igrac2");
            if ("CRVENA".equals(mapiraneBoje.get(redoslijed.get(1))))
                igrac2.setTextFill(Color.RED);
            else if ("PLAVA".equals(mapiraneBoje.get(redoslijed.get(1))))
                igrac2.setTextFill(Color.BLUE);
            else if ("ZELENA".equals(mapiraneBoje.get(redoslijed.get(1))))
                igrac2.setTextFill(Color.GREEN);
            else if ("ZUTA".equals(mapiraneBoje.get(redoslijed.get(1))))
                igrac2.setTextFill(Color.YELLOW);

            igrac3.setText("Igrac3");
            if ("CRVENA".equals(mapiraneBoje.get(redoslijed.get(2))))
                igrac3.setTextFill(Color.RED);
            else if ("PLAVA".equals(mapiraneBoje.get(redoslijed.get(2))))
                igrac3.setTextFill(Color.BLUE);
            else if ("ZELENA".equals(mapiraneBoje.get(redoslijed.get(2))))
                igrac3.setTextFill(Color.GREEN);
            else if ("ZUTA".equals(mapiraneBoje.get(redoslijed.get(2))))
                igrac3.setTextFill(Color.YELLOW);

            igrac4.setText("Igrac4");
            if ("CRVENA".equals(mapiraneBoje.get(redoslijed.get(3))))
                igrac4.setTextFill(Color.RED);
            else if ("PLAVA".equals(mapiraneBoje.get(redoslijed.get(3))))
                igrac4.setTextFill(Color.BLUE);
            else if ("ZELENA".equals(mapiraneBoje.get(redoslijed.get(3))))
                igrac4.setTextFill(Color.GREEN);
            else if ("ZUTA".equals(mapiraneBoje.get(redoslijed.get(3))))
                igrac4.setTextFill(Color.YELLOW);
        }
    }
    private void kreiranjeSpila() {
        for (int i = 0; i < 10; i++)
           spil.add(new ObicnaKarta(1));
        for (int i = 0; i < 10; i++)
            spil.add(new ObicnaKarta(2));
        for (int i = 0; i < 10; i++)
         spil.add(new ObicnaKarta(3));
        for (int i = 0; i < 10; i++)
           spil.add(new ObicnaKarta(4));
        for (int i = 0; i < 12; i++)
            spil.add(new SpecijalnaKarta());
    }
    private ArrayList<Figura> kreiranjeFigura(int oznaka){//oznaka je redni broj igraca
        ArrayList<Figura>figuras=new ArrayList<Figura>();
        int brojac=0;
        while(brojac!=4){
            int broj=rand.nextInt(3);
            if(broj==0) {
                ObicnaFigura obicnaFigura=new ObicnaFigura(mapiraneBoje.get(redoslijed.get(oznaka)));
                obicnaFigura.setPozicijaUIstoriji(brojacIstorije);
                brojacIstorije++;
                sveFigure.add(obicnaFigura);
                figuras.add(obicnaFigura);
                brojac++;
            }
            else if(broj==1) {
                SuperBrzaFigura superBrzaFigura=new SuperBrzaFigura(mapiraneBoje.get(redoslijed.get(oznaka)));
                superBrzaFigura.setPozicijaUIstoriji(brojacIstorije);
                brojacIstorije++;
                sveFigure.add(superBrzaFigura);
                figuras.add(superBrzaFigura);
                brojac++;
            }
            else if(broj==2) {
                LebdecaFigura lebdecaFigura=new LebdecaFigura(mapiraneBoje.get(redoslijed.get(oznaka)));
                lebdecaFigura.setPozicijaUIstoriji(brojacIstorije);
                brojacIstorije++;
                sveFigure.add(lebdecaFigura);
                figuras.add(lebdecaFigura);
                brojac++;
            }
        }
        return figuras;
    }


    private void kreiranjeIgraca() {
        if(brojIgraca==2){
            igraci.add(new Igrac("Aleksandar",kreiranjeFigura(0)));
            igraci.add(new Igrac("Dajan",kreiranjeFigura(1)));
            igraci.get(0).setRedniBrojURedoslijedu(redoslijed.get(0));
            igraci.get(1).setRedniBrojURedoslijedu(redoslijed.get(1));
        } else if(brojIgraca==3){
            igraci.add(new Igrac("Aleksandar",kreiranjeFigura(0)));
            igraci.add(new Igrac("Dajan",kreiranjeFigura(1)));
            igraci.add(new Igrac("Bojan",kreiranjeFigura(2)));
            igraci.get(0).setRedniBrojURedoslijedu(redoslijed.get(0));
            igraci.get(1).setRedniBrojURedoslijedu(redoslijed.get(1));
            igraci.get(2).setRedniBrojURedoslijedu(redoslijed.get(2));
        }else if(brojIgraca==4){
            igraci.add(new Igrac("Aleksandar",kreiranjeFigura(0)));
            igraci.add(new Igrac("Dajan",kreiranjeFigura(1)));
            igraci.add(new Igrac("Bojan",kreiranjeFigura(2)));
            igraci.add(new Igrac("Srdjan",kreiranjeFigura(3)));
            igraci.get(0).setRedniBrojURedoslijedu(redoslijed.get(0));
            igraci.get(1).setRedniBrojURedoslijedu(redoslijed.get(1));
            igraci.get(2).setRedniBrojURedoslijedu(redoslijed.get(2));
            igraci.get(3).setRedniBrojURedoslijedu(redoslijed.get(3));
        }
    }

    public static void postavljanjeRupa() {

        int brojac=0;
        int dim=Figura.putanjaKretanja.size()-1;

                while (brojac != brojRupa) {
                    int var=rand.nextInt(dim)+2;
                        if(var%2==1) {
                            if (!mapa.getElementMape(Figura.putanjaKretanja.get(var), Figura.putanjaKretanja.get(var-1)).isRupa()) {
                                mapa.getElementMape(Figura.putanjaKretanja.get(var), Figura.putanjaKretanja.get(var-1)).setRupa(true);
                                brojac++;
                            }
                        }
                }
    }

    public static void akcijaRupe(){
        for(int i= 0;i<dimenzijeMatrice;i++){
            for(int j=0;j<dimenzijeMatrice;j++){
                if(mapa.getElementMape(i,j).getSadrzajPolja()!=null && mapa.getElementMape(i,j).isRupa()){
                    if(mapa.getElementMape(i,j).getSadrzajPolja() instanceof ObicnaFigura ||
                            mapa.getElementMape(i,j).getSadrzajPolja() instanceof SuperBrzaFigura ){
                        porukaNaKonzoli="Propada Figura";
                        mapa.getElementMape(i,j).getSadrzajPolja().setUpalaUrupu(true);
                        mapa.getElementMape(i,j).getSadrzajPolja().setZavrsio(true);
                        mapa.getElementMape(i,j).setSadrzajPolja(null);

                    }
                }
            }
        }
        for(int i= 0;i<dimenzijeMatrice;i++){
            for(int j=0;j<dimenzijeMatrice;j++){
                mapa.getElementMape(i,j).setRupa(false);
            }
        }
    }

        public void prikazIstorije(ActionEvent event) {
        try {
            Button soruceButton=(Button)event.getSource();
            if(soruceButton.equals(figura1))
                    PrikazKretanjaController.putanja=predjeniPuteviFigura.get(0);
            else if(soruceButton.equals(figura2))
                PrikazKretanjaController.putanja=predjeniPuteviFigura.get(1);
            else if(soruceButton.equals(figura3))
                PrikazKretanjaController.putanja=predjeniPuteviFigura.get(2);
            else if(soruceButton.equals(figura4))
                PrikazKretanjaController.putanja=predjeniPuteviFigura.get(3);
            else if(soruceButton.equals(figura5))
                PrikazKretanjaController.putanja=predjeniPuteviFigura.get(4);
            else if(soruceButton.equals(figura6))
                PrikazKretanjaController.putanja=predjeniPuteviFigura.get(5);
            else if(soruceButton.equals(figura7))
                PrikazKretanjaController.putanja=predjeniPuteviFigura.get(6);
            else if(soruceButton.equals(figura8))
                PrikazKretanjaController.putanja=predjeniPuteviFigura.get(7);
            else if(soruceButton.equals(figura9))
                PrikazKretanjaController.putanja=predjeniPuteviFigura.get(8);
            else if(soruceButton.equals(figura10))
                PrikazKretanjaController.putanja=predjeniPuteviFigura.get(9);
            else if(soruceButton.equals(figura11))
                PrikazKretanjaController.putanja=predjeniPuteviFigura.get(10);
            else if(soruceButton.equals(figura12))
                PrikazKretanjaController.putanja=predjeniPuteviFigura.get(11);
            else if(soruceButton.equals(figura13))
                PrikazKretanjaController.putanja=predjeniPuteviFigura.get(12);
            else if(soruceButton.equals(figura14))
                PrikazKretanjaController.putanja=predjeniPuteviFigura.get(13);
            else if(soruceButton.equals(figura15))
                PrikazKretanjaController.putanja=predjeniPuteviFigura.get(14);
            else if(soruceButton.equals(figura16))
                PrikazKretanjaController.putanja=predjeniPuteviFigura.get(15);



            FXMLLoader loader=new FXMLLoader(getClass().getResource("/fxml/prikazKretanja.fxml"));
            Parent root=loader.load();
            prikazKretanjaController=new PrikazKretanjaController();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            Image icon=new Image("icons/game.png");
            stage.getIcons().add(icon);
            stage.show();
        }catch(Exception e){
            Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
        }

    }

    public void prikazRezultata(ActionEvent event){
        try{
        FXMLLoader loader=new FXMLLoader(getClass().getResource("/fxml/prikazRezultata.fxml"));
        Parent root=loader.load();
        PrikazRezultataController prikazRezultataController=new PrikazRezultataController();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        Image icon=new Image("icons/game.png");
        stage.getIcons().add(icon);
        stage.show();
    }catch(Exception e){
        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
    }

}

    private void kreirajFajlove(){
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd-MM-yyyy---HH-mm-ss");
        Date date=new Date();
        String time=simpleDateFormat.format(date);
        try{
            File file=new File("src//rezultati//IGRA_"+time+".txt");
            file.createNewFile();
            PrintWriter pw=new PrintWriter(file);
            for(int i=0;i<igraci.size();i++){
                Igrac igrac=igraci.get(i);
                pw.println("Igrac "+(i+1)+" - "+igrac.getIme());
                for(int j=0;j<4;j++){
                    Figura figura=igrac.getFigureIgracal().get(j);
                    String stiglaDoCilja="";
                    if(figura.isUpalaUrupu())
                        stiglaDoCilja="ne";
                    else
                        stiglaDoCilja="da";
                    pw.println("Figura "+figura.getPozicijaUIstoriji()+" "+"( "+figura.getNazivFigure()+" "+figura.getBoja()+" )"+
                              " predjeni put ( "+Arrays.toString((predjeniPuteviFigura.get(figura.getPozicijaUIstoriji())).toArray())+" )"+
                            " - stigla do cilja ("+stiglaDoCilja+")" );

                    pw.flush();
                }


            }
            pw.println("Ukupno vrijeme trajanja igre je: "+(System.currentTimeMillis()-pocetno_vrijeme)/1000+" s.");

            pw.close();
        }catch (Exception e){
            Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
        }

    }
    /*
    public void prikazFajlova(ActionEvent event)
    {
        FileChooser fileChooser=new FileChooser();
        fileChooser.setInitialDirectory(new File(PUTANJA_REZULTATI));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files","*.txt"));
        File file =  fileChooser.showOpenDialog(null);
        if(!Desktop.isDesktopSupported()){System.out.println("Error"); return;}
        Desktop desktop = Desktop.getDesktop();
        if(file.exists())
        {
            try {
                desktop.open(file);
            }catch (IOException e)
            {
                Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
            }
        }
    }
    */


    private void kreiranjeMapePredjenihPutevaFigura(){
        predjeniPuteviFigura.put(0,new ArrayList<Integer>());
        predjeniPuteviFigura.put(1,new ArrayList<Integer>());
        predjeniPuteviFigura.put(2,new ArrayList<Integer>());
        predjeniPuteviFigura.put(3,new ArrayList<Integer>());
        predjeniPuteviFigura.put(4,new ArrayList<Integer>());
        predjeniPuteviFigura.put(5,new ArrayList<Integer>());
        predjeniPuteviFigura.put(6,new ArrayList<Integer>());
        predjeniPuteviFigura.put(7,new ArrayList<Integer>());
        predjeniPuteviFigura.put(8,new ArrayList<Integer>());
        predjeniPuteviFigura.put(9,new ArrayList<Integer>());
        predjeniPuteviFigura.put(10,new ArrayList<Integer>());
        predjeniPuteviFigura.put(11,new ArrayList<Integer>());
        predjeniPuteviFigura.put(12,new ArrayList<Integer>());
        predjeniPuteviFigura.put(13,new ArrayList<Integer>());
        predjeniPuteviFigura.put(14,new ArrayList<Integer>());
        predjeniPuteviFigura.put(15,new ArrayList<Integer>());
    }
    public void pauza(ActionEvent actionEvent) {
        if(PAUZA) {
            synchronized (Mapa.lock) {
                PAUZA = false;
                Mapa.lock.notifyAll();
            }
        }
        else {
            synchronized (Mapa.lock) {
                PAUZA = true;
                Mapa.lock.notifyAll();
            }
        }
    }

    public static boolean provjeraKrajaAplikacije(){
        for(int i=0;i<sveFigure.size();i++){
            if(!sveFigure.get(i).isZavrsio())
                return false;
        }
        return true;
    }

    public static void skiniDijamante(){
        for(int i=0;i<dimenzijeMatrice;i++){
            for(int j=0;j<dimenzijeMatrice;j++){
                mapa.getElementMape(i,j).setDijamant(false);
            }
        }
    }

    private void podesavanjeIkonicaFigura(){
        if(sveFigure.get(0) instanceof LebdecaFigura){
            if("CRVENA".equals(sveFigure.get(0).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_CRVENA);
                    Image image=new Image(fileInputStream);
                    figurica1.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else  if("PLAVA".equals(sveFigure.get(0).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_PLAVA);
                    Image image=new Image(fileInputStream);
                    figurica1.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZUTA".equals(sveFigure.get(0).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZUTA);
                    Image image=new Image(fileInputStream);
                    figurica1.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZELENA".equals(sveFigure.get(0).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZELENA);
                    Image image=new Image(fileInputStream);
                    figurica1.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }
        }else if(sveFigure.get(0) instanceof ObicnaFigura){
            if("CRVENA".equals(sveFigure.get(0).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_CRVENA);
                    Image image=new Image(fileInputStream);
                    figurica1.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else  if("PLAVA".equals(sveFigure.get(0).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_PLAVA);
                    Image image=new Image(fileInputStream);
                    figurica1.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZUTA".equals(sveFigure.get(0).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZUTA);
                    Image image=new Image(fileInputStream);
                    figurica1.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZELENA".equals(sveFigure.get(0).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZELENA);
                    Image image=new Image(fileInputStream);
                    figurica1.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }
        }else if(sveFigure.get(0) instanceof SuperBrzaFigura){
            if("CRVENA".equals(sveFigure.get(0).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_SUPER_BRZA_CRVENA);
                    Image image=new Image(fileInputStream);
                    figurica1.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else  if("PLAVA".equals(sveFigure.get(0).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_SUPER_BRZA_PLAVA);
                    Image image=new Image(fileInputStream);
                    figurica1.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZUTA".equals(sveFigure.get(0).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZUTA);
                    Image image=new Image(fileInputStream);
                    figurica1.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZELENA".equals(sveFigure.get(0).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZELENA);
                    Image image=new Image(fileInputStream);
                    figurica1.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }
        }

    ///////////////////////
        if(sveFigure.get(1) instanceof LebdecaFigura){
            if("CRVENA".equals(sveFigure.get(1).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_CRVENA);
                    Image image=new Image(fileInputStream);
                    figurica2.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else  if("PLAVA".equals(sveFigure.get(1).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_PLAVA);
                    Image image=new Image(fileInputStream);
                    figurica2.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZUTA".equals(sveFigure.get(1).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZUTA);
                    Image image=new Image(fileInputStream);
                    figurica2.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZELENA".equals(sveFigure.get(1).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZELENA);
                    Image image=new Image(fileInputStream);
                    figurica2.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }
        }else if(sveFigure.get(1) instanceof ObicnaFigura){
            if("CRVENA".equals(sveFigure.get(1).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_CRVENA);
                    Image image=new Image(fileInputStream);
                    figurica2.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else  if("PLAVA".equals(sveFigure.get(1).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_PLAVA);
                    Image image=new Image(fileInputStream);
                    figurica2.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZUTA".equals(sveFigure.get(1).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZUTA);
                    Image image=new Image(fileInputStream);
                    figurica2.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZELENA".equals(sveFigure.get(1).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZELENA);
                    Image image=new Image(fileInputStream);
                    figurica2.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }
        }else if(sveFigure.get(1) instanceof SuperBrzaFigura) {
            if ("CRVENA".equals(sveFigure.get(1).getBoja())) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_CRVENA);
                    Image image = new Image(fileInputStream);
                    figurica2.setImage(image);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else if ("PLAVA".equals(sveFigure.get(1).getBoja())) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_PLAVA);
                    Image image = new Image(fileInputStream);
                    figurica2.setImage(image);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else if ("ZUTA".equals(sveFigure.get(1).getBoja())) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZUTA);
                    Image image = new Image(fileInputStream);
                    figurica2.setImage(image);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else if ("ZELENA".equals(sveFigure.get(1).getBoja())) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZELENA);
                    Image image = new Image(fileInputStream);
                    figurica2.setImage(image);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }

        }
        ////////////
        if(sveFigure.get(2) instanceof LebdecaFigura){
            if("CRVENA".equals(sveFigure.get(2).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_CRVENA);
                    Image image=new Image(fileInputStream);
                    figurica3.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else  if("PLAVA".equals(sveFigure.get(2).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_PLAVA);
                    Image image=new Image(fileInputStream);
                    figurica3.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZUTA".equals(sveFigure.get(2).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZUTA);
                    Image image=new Image(fileInputStream);
                    figurica3.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZELENA".equals(sveFigure.get(2).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZELENA);
                    Image image=new Image(fileInputStream);
                    figurica3.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }
        }else if(sveFigure.get(2) instanceof ObicnaFigura){
            if("CRVENA".equals(sveFigure.get(2).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_CRVENA);
                    Image image=new Image(fileInputStream);
                    figurica3.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else  if("PLAVA".equals(sveFigure.get(2).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_PLAVA);
                    Image image=new Image(fileInputStream);
                    figurica3.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZUTA".equals(sveFigure.get(2).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZUTA);
                    Image image=new Image(fileInputStream);
                    figurica3.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZELENA".equals(sveFigure.get(2).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZELENA);
                    Image image=new Image(fileInputStream);
                    figurica3.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }
        }else if(sveFigure.get(2) instanceof SuperBrzaFigura) {
            if ("CRVENA".equals(sveFigure.get(2).getBoja())) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_CRVENA);
                    Image image = new Image(fileInputStream);
                    figurica3.setImage(image);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else if ("PLAVA".equals(sveFigure.get(2).getBoja())) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_PLAVA);
                    Image image = new Image(fileInputStream);
                    figurica3.setImage(image);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else if ("ZUTA".equals(sveFigure.get(2).getBoja())) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZUTA);
                    Image image = new Image(fileInputStream);
                    figurica3.setImage(image);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else if ("ZELENA".equals(sveFigure.get(2).getBoja())) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZELENA);
                    Image image = new Image(fileInputStream);
                    figurica3.setImage(image);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }

        }
        /////////////////////
        if(sveFigure.get(3) instanceof LebdecaFigura){
            if("CRVENA".equals(sveFigure.get(3).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_CRVENA);
                    Image image=new Image(fileInputStream);
                    figurica4.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else  if("PLAVA".equals(sveFigure.get(3).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_PLAVA);
                    Image image=new Image(fileInputStream);
                    figurica4.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZUTA".equals(sveFigure.get(3).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZUTA);
                    Image image=new Image(fileInputStream);
                    figurica4.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZELENA".equals(sveFigure.get(3).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZELENA);
                    Image image=new Image(fileInputStream);
                    figurica4.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }
        }else if(sveFigure.get(3) instanceof ObicnaFigura){
            if("CRVENA".equals(sveFigure.get(3).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_CRVENA);
                    Image image=new Image(fileInputStream);
                    figurica4.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else  if("PLAVA".equals(sveFigure.get(3).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_PLAVA);
                    Image image=new Image(fileInputStream);
                    figurica4.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZUTA".equals(sveFigure.get(3).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZUTA);
                    Image image=new Image(fileInputStream);
                    figurica4.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZELENA".equals(sveFigure.get(3).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZELENA);
                    Image image=new Image(fileInputStream);
                    figurica4.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }
        }else if(sveFigure.get(3) instanceof SuperBrzaFigura) {
            if ("CRVENA".equals(sveFigure.get(3).getBoja())) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_CRVENA);
                    Image image = new Image(fileInputStream);
                    figurica4.setImage(image);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else if ("PLAVA".equals(sveFigure.get(3).getBoja())) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_PLAVA);
                    Image image = new Image(fileInputStream);
                    figurica4.setImage(image);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else if ("ZUTA".equals(sveFigure.get(3).getBoja())) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZUTA);
                    Image image = new Image(fileInputStream);
                    figurica4.setImage(image);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else if ("ZELENA".equals(sveFigure.get(3).getBoja())) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZELENA);
                    Image image = new Image(fileInputStream);
                    figurica4.setImage(image);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }

        }
        //////////////////////
        if(sveFigure.get(4) instanceof LebdecaFigura){
            if("CRVENA".equals(sveFigure.get(4).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_CRVENA);
                    Image image=new Image(fileInputStream);
                    figurica5.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else  if("PLAVA".equals(sveFigure.get(4).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_PLAVA);
                    Image image=new Image(fileInputStream);
                    figurica5.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZUTA".equals(sveFigure.get(4).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZUTA);
                    Image image=new Image(fileInputStream);
                    figurica5.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZELENA".equals(sveFigure.get(4).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZELENA);
                    Image image=new Image(fileInputStream);
                    figurica5.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }
        }else if(sveFigure.get(4) instanceof ObicnaFigura){
            if("CRVENA".equals(sveFigure.get(4).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_CRVENA);
                    Image image=new Image(fileInputStream);
                    figurica5.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else  if("PLAVA".equals(sveFigure.get(4).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_PLAVA);
                    Image image=new Image(fileInputStream);
                    figurica5.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZUTA".equals(sveFigure.get(4).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZUTA);
                    Image image=new Image(fileInputStream);
                    figurica5.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZELENA".equals(sveFigure.get(4).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZELENA);
                    Image image=new Image(fileInputStream);
                    figurica5.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }
        }else if(sveFigure.get(4) instanceof SuperBrzaFigura) {
            if ("CRVENA".equals(sveFigure.get(4).getBoja())) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_CRVENA);
                    Image image = new Image(fileInputStream);
                    figurica5.setImage(image);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else if ("PLAVA".equals(sveFigure.get(4).getBoja())) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_PLAVA);
                    Image image = new Image(fileInputStream);
                    figurica5.setImage(image);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else if ("ZUTA".equals(sveFigure.get(4).getBoja())) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZUTA);
                    Image image = new Image(fileInputStream);
                    figurica5.setImage(image);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else if ("ZELENA".equals(sveFigure.get(4).getBoja())) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZELENA);
                    Image image = new Image(fileInputStream);
                    figurica5.setImage(image);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }

        }
        ////////
        if(sveFigure.get(5) instanceof LebdecaFigura){
            if("CRVENA".equals(sveFigure.get(5).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_CRVENA);
                    Image image=new Image(fileInputStream);
                    figurica6.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else  if("PLAVA".equals(sveFigure.get(5).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_PLAVA);
                    Image image=new Image(fileInputStream);
                    figurica6.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZUTA".equals(sveFigure.get(5).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZUTA);
                    Image image=new Image(fileInputStream);
                    figurica6.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZELENA".equals(sveFigure.get(5).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZELENA);
                    Image image=new Image(fileInputStream);
                    figurica6.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }
        }else if(sveFigure.get(5) instanceof ObicnaFigura){
            if("CRVENA".equals(sveFigure.get(5).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_CRVENA);
                    Image image=new Image(fileInputStream);
                    figurica6.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else  if("PLAVA".equals(sveFigure.get(5).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_PLAVA);
                    Image image=new Image(fileInputStream);
                    figurica6.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZUTA".equals(sveFigure.get(5).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZUTA);
                    Image image=new Image(fileInputStream);
                    figurica6.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZELENA".equals(sveFigure.get(5).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZELENA);
                    Image image=new Image(fileInputStream);
                    figurica6.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }
        }else if(sveFigure.get(5) instanceof SuperBrzaFigura) {
            if ("CRVENA".equals(sveFigure.get(5).getBoja())) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_CRVENA);
                    Image image = new Image(fileInputStream);
                    figurica6.setImage(image);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else if ("PLAVA".equals(sveFigure.get(5).getBoja())) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_PLAVA);
                    Image image = new Image(fileInputStream);
                    figurica6.setImage(image);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else if ("ZUTA".equals(sveFigure.get(5).getBoja())) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZUTA);
                    Image image = new Image(fileInputStream);
                    figurica6.setImage(image);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else if ("ZELENA".equals(sveFigure.get(5).getBoja())) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZELENA);
                    Image image = new Image(fileInputStream);
                    figurica6.setImage(image);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }

        }
        ///////////
        if(sveFigure.get(6) instanceof LebdecaFigura){
            if("CRVENA".equals(sveFigure.get(6).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_CRVENA);
                    Image image=new Image(fileInputStream);
                    figurica7.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else  if("PLAVA".equals(sveFigure.get(6).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_PLAVA);
                    Image image=new Image(fileInputStream);
                    figurica7.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZUTA".equals(sveFigure.get(6).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZUTA);
                    Image image=new Image(fileInputStream);
                    figurica7.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZELENA".equals(sveFigure.get(6).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZELENA);
                    Image image=new Image(fileInputStream);
                    figurica7.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }
        }else if(sveFigure.get(6) instanceof ObicnaFigura){
            if("CRVENA".equals(sveFigure.get(6).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_CRVENA);
                    Image image=new Image(fileInputStream);
                    figurica7.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else  if("PLAVA".equals(sveFigure.get(6).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_PLAVA);
                    Image image=new Image(fileInputStream);
                    figurica7.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZUTA".equals(sveFigure.get(6).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZUTA);
                    Image image=new Image(fileInputStream);
                    figurica7.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZELENA".equals(sveFigure.get(6).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZELENA);
                    Image image=new Image(fileInputStream);
                    figurica7.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }
        }else if(sveFigure.get(6) instanceof SuperBrzaFigura) {
            if ("CRVENA".equals(sveFigure.get(6).getBoja())) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_CRVENA);
                    Image image = new Image(fileInputStream);
                    figurica7.setImage(image);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else if ("PLAVA".equals(sveFigure.get(6).getBoja())) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_PLAVA);
                    Image image = new Image(fileInputStream);
                    figurica7.setImage(image);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else if ("ZUTA".equals(sveFigure.get(6).getBoja())) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZUTA);
                    Image image = new Image(fileInputStream);
                    figurica7.setImage(image);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else if ("ZELENA".equals(sveFigure.get(6).getBoja())) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZELENA);
                    Image image = new Image(fileInputStream);
                    figurica7.setImage(image);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }

        }
        //////////////
        if(sveFigure.get(7) instanceof LebdecaFigura){
            if("CRVENA".equals(sveFigure.get(7).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_CRVENA);
                    Image image=new Image(fileInputStream);
                    figurica8.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else  if("PLAVA".equals(sveFigure.get(7).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_PLAVA);
                    Image image=new Image(fileInputStream);
                    figurica8.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZUTA".equals(sveFigure.get(7).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZUTA);
                    Image image=new Image(fileInputStream);
                    figurica8.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZELENA".equals(sveFigure.get(7).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZELENA);
                    Image image=new Image(fileInputStream);
                    figurica8.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }
        }else if(sveFigure.get(7) instanceof ObicnaFigura){
            if("CRVENA".equals(sveFigure.get(7).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_CRVENA);
                    Image image=new Image(fileInputStream);
                    figurica8.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else  if("PLAVA".equals(sveFigure.get(7).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_PLAVA);
                    Image image=new Image(fileInputStream);
                    figurica8.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZUTA".equals(sveFigure.get(7).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZUTA);
                    Image image=new Image(fileInputStream);
                    figurica8.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }else  if("ZELENA".equals(sveFigure.get(7).getBoja())){
                try{
                    FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZELENA);
                    Image image=new Image(fileInputStream);
                    figurica8.setImage(image);
                }catch (Exception e){
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }
        }else if(sveFigure.get(7) instanceof SuperBrzaFigura) {
            if ("CRVENA".equals(sveFigure.get(7).getBoja())) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_CRVENA);
                    Image image = new Image(fileInputStream);
                    figurica8.setImage(image);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else if ("PLAVA".equals(sveFigure.get(7).getBoja())) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_PLAVA);
                    Image image = new Image(fileInputStream);
                    figurica8.setImage(image);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else if ("ZUTA".equals(sveFigure.get(7).getBoja())) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZUTA);
                    Image image = new Image(fileInputStream);
                    figurica8.setImage(image);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            } else if ("ZELENA".equals(sveFigure.get(7).getBoja())) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZELENA);
                    Image image = new Image(fileInputStream);
                    figurica8.setImage(image);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }

        }
        ////
        if(brojIgraca>=3){
            if(sveFigure.get(8) instanceof LebdecaFigura){
                if("CRVENA".equals(sveFigure.get(8).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_CRVENA);
                        Image image=new Image(fileInputStream);
                        figurica9.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else  if("PLAVA".equals(sveFigure.get(8).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_PLAVA);
                        Image image=new Image(fileInputStream);
                        figurica9.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZUTA".equals(sveFigure.get(8).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZUTA);
                        Image image=new Image(fileInputStream);
                        figurica9.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZELENA".equals(sveFigure.get(8).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZELENA);
                        Image image=new Image(fileInputStream);
                        figurica9.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }
            }else if(sveFigure.get(8) instanceof ObicnaFigura){
                if("CRVENA".equals(sveFigure.get(8).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_CRVENA);
                        Image image=new Image(fileInputStream);
                        figurica9.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else  if("PLAVA".equals(sveFigure.get(8).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_PLAVA);
                        Image image=new Image(fileInputStream);
                        figurica9.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZUTA".equals(sveFigure.get(8).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZUTA);
                        Image image=new Image(fileInputStream);
                        figurica9.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZELENA".equals(sveFigure.get(8).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZELENA);
                        Image image=new Image(fileInputStream);
                        figurica9.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }
            }else if(sveFigure.get(8) instanceof SuperBrzaFigura) {
                if ("CRVENA".equals(sveFigure.get(8).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_CRVENA);
                        Image image = new Image(fileInputStream);
                        figurica9.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else if ("PLAVA".equals(sveFigure.get(8).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_PLAVA);
                        Image image = new Image(fileInputStream);
                        figurica9.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else if ("ZUTA".equals(sveFigure.get(8).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZUTA);
                        Image image = new Image(fileInputStream);
                        figurica9.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else if ("ZELENA".equals(sveFigure.get(8).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZELENA);
                        Image image = new Image(fileInputStream);
                        figurica9.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }

            }
            ////
            if(sveFigure.get(9) instanceof LebdecaFigura){
                if("CRVENA".equals(sveFigure.get(9).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_CRVENA);
                        Image image=new Image(fileInputStream);
                        figurica10.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else  if("PLAVA".equals(sveFigure.get(9).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_PLAVA);
                        Image image=new Image(fileInputStream);
                        figurica10.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);}
                }else  if("ZUTA".equals(sveFigure.get(9).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZUTA);
                        Image image=new Image(fileInputStream);
                        figurica10.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZELENA".equals(sveFigure.get(9).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZELENA);
                        Image image=new Image(fileInputStream);
                        figurica10.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);}
                }
            }else if(sveFigure.get(9) instanceof ObicnaFigura){
                if("CRVENA".equals(sveFigure.get(9).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_CRVENA);
                        Image image=new Image(fileInputStream);
                        figurica10.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else  if("PLAVA".equals(sveFigure.get(9).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_PLAVA);
                        Image image=new Image(fileInputStream);
                        figurica10.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZUTA".equals(sveFigure.get(9).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZUTA);
                        Image image=new Image(fileInputStream);
                        figurica10.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZELENA".equals(sveFigure.get(9).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZELENA);
                        Image image=new Image(fileInputStream);
                        figurica10.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }
            }else if(sveFigure.get(9) instanceof SuperBrzaFigura) {
                if ("CRVENA".equals(sveFigure.get(9).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_CRVENA);
                        Image image = new Image(fileInputStream);
                        figurica10.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else if ("PLAVA".equals(sveFigure.get(9).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_PLAVA);
                        Image image = new Image(fileInputStream);
                        figurica10.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else if ("ZUTA".equals(sveFigure.get(9).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZUTA);
                        Image image = new Image(fileInputStream);
                        figurica10.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else if ("ZELENA".equals(sveFigure.get(9).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZELENA);
                        Image image = new Image(fileInputStream);
                        figurica10.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }

            }
            /////
            if(sveFigure.get(10) instanceof LebdecaFigura){
                if("CRVENA".equals(sveFigure.get(10).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_CRVENA);
                        Image image=new Image(fileInputStream);
                        figurica11.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else  if("PLAVA".equals(sveFigure.get(10).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_PLAVA);
                        Image image=new Image(fileInputStream);
                        figurica11.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZUTA".equals(sveFigure.get(10).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZUTA);
                        Image image=new Image(fileInputStream);
                        figurica11.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZELENA".equals(sveFigure.get(10).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZELENA);
                        Image image=new Image(fileInputStream);
                        figurica11.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }
            }else if(sveFigure.get(10) instanceof ObicnaFigura){
                if("CRVENA".equals(sveFigure.get(10).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_CRVENA);
                        Image image=new Image(fileInputStream);
                        figurica11.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else  if("PLAVA".equals(sveFigure.get(10).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_PLAVA);
                        Image image=new Image(fileInputStream);
                        figurica11.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZUTA".equals(sveFigure.get(10).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZUTA);
                        Image image=new Image(fileInputStream);
                        figurica11.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZELENA".equals(sveFigure.get(10).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZELENA);
                        Image image=new Image(fileInputStream);
                        figurica11.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }
            }else if(sveFigure.get(10) instanceof SuperBrzaFigura) {
                if ("CRVENA".equals(sveFigure.get(10).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_CRVENA);
                        Image image = new Image(fileInputStream);
                        figurica11.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else if ("PLAVA".equals(sveFigure.get(10).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_PLAVA);
                        Image image = new Image(fileInputStream);
                        figurica11.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else if ("ZUTA".equals(sveFigure.get(10).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZUTA);
                        Image image = new Image(fileInputStream);
                        figurica11.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else if ("ZELENA".equals(sveFigure.get(10).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZELENA);
                        Image image = new Image(fileInputStream);
                        figurica11.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }

            }
            ////
            if(sveFigure.get(11) instanceof LebdecaFigura){
                if("CRVENA".equals(sveFigure.get(11).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_CRVENA);
                        Image image=new Image(fileInputStream);
                        figurica12.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else  if("PLAVA".equals(sveFigure.get(11).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_PLAVA);
                        Image image=new Image(fileInputStream);
                        figurica12.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZUTA".equals(sveFigure.get(11).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZUTA);
                        Image image=new Image(fileInputStream);
                        figurica12.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZELENA".equals(sveFigure.get(11).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZELENA);
                        Image image=new Image(fileInputStream);
                        figurica12.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }
            }else if(sveFigure.get(11) instanceof ObicnaFigura){
                if("CRVENA".equals(sveFigure.get(11).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_CRVENA);
                        Image image=new Image(fileInputStream);
                        figurica12.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else  if("PLAVA".equals(sveFigure.get(11).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_PLAVA);
                        Image image=new Image(fileInputStream);
                        figurica12.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZUTA".equals(sveFigure.get(11).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZUTA);
                        Image image=new Image(fileInputStream);
                        figurica12.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZELENA".equals(sveFigure.get(11).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZELENA);
                        Image image=new Image(fileInputStream);
                        figurica12.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }
            }else if(sveFigure.get(11) instanceof SuperBrzaFigura) {
                if ("CRVENA".equals(sveFigure.get(11).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_CRVENA);
                        Image image = new Image(fileInputStream);
                        figurica12.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else if ("PLAVA".equals(sveFigure.get(11).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_PLAVA);
                        Image image = new Image(fileInputStream);
                        figurica12.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else if ("ZUTA".equals(sveFigure.get(11).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZUTA);
                        Image image = new Image(fileInputStream);
                        figurica12.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else if ("ZELENA".equals(sveFigure.get(11).getBoja())) {
                 try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZELENA);
                        Image image = new Image(fileInputStream);
                        figurica12.setImage(image);
                    } catch (Exception e) {
                     Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }

            }
        }
        if(brojIgraca==4){
            if(sveFigure.get(12) instanceof LebdecaFigura){
                if("CRVENA".equals(sveFigure.get(12).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_CRVENA);
                        Image image=new Image(fileInputStream);
                        figurica13.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else  if("PLAVA".equals(sveFigure.get(12).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_PLAVA);
                        Image image=new Image(fileInputStream);
                        figurica13.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZUTA".equals(sveFigure.get(12).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZUTA);
                        Image image=new Image(fileInputStream);
                        figurica13.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZELENA".equals(sveFigure.get(12).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZELENA);
                        Image image=new Image(fileInputStream);
                        figurica13.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }
            }else if(sveFigure.get(12) instanceof ObicnaFigura){
                if("CRVENA".equals(sveFigure.get(12).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_CRVENA);
                        Image image=new Image(fileInputStream);
                        figurica13.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else  if("PLAVA".equals(sveFigure.get(12).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_PLAVA);
                        Image image=new Image(fileInputStream);
                        figurica13.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZUTA".equals(sveFigure.get(12).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZUTA);
                        Image image=new Image(fileInputStream);
                        figurica13.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZELENA".equals(sveFigure.get(12).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZELENA);
                        Image image=new Image(fileInputStream);
                        figurica13.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }
            }else if(sveFigure.get(12) instanceof SuperBrzaFigura) {
                if ("CRVENA".equals(sveFigure.get(12).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_CRVENA);
                        Image image = new Image(fileInputStream);
                        figurica13.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else if ("PLAVA".equals(sveFigure.get(12).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_PLAVA);
                        Image image = new Image(fileInputStream);
                        figurica13.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else if ("ZUTA".equals(sveFigure.get(12).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZUTA);
                        Image image = new Image(fileInputStream);
                        figurica13.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else if ("ZELENA".equals(sveFigure.get(12).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZELENA);
                        Image image = new Image(fileInputStream);
                        figurica13.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }

            }
            ///
            if(sveFigure.get(13) instanceof LebdecaFigura){
                if("CRVENA".equals(sveFigure.get(13).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_CRVENA);
                        Image image=new Image(fileInputStream);
                        figurica14.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else  if("PLAVA".equals(sveFigure.get(13).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_PLAVA);
                        Image image=new Image(fileInputStream);
                        figurica14.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZUTA".equals(sveFigure.get(13).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZUTA);
                        Image image=new Image(fileInputStream);
                        figurica14.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZELENA".equals(sveFigure.get(13).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZELENA);
                        Image image=new Image(fileInputStream);
                        figurica14.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }
            }else if(sveFigure.get(13) instanceof ObicnaFigura){
                if("CRVENA".equals(sveFigure.get(13).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_CRVENA);
                        Image image=new Image(fileInputStream);
                        figurica14.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else  if("PLAVA".equals(sveFigure.get(13).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_PLAVA);
                        Image image=new Image(fileInputStream);
                        figurica14.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZUTA".equals(sveFigure.get(13).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZUTA);
                        Image image=new Image(fileInputStream);
                        figurica14.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZELENA".equals(sveFigure.get(13).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZELENA);
                        Image image=new Image(fileInputStream);
                        figurica14.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }
            }else if(sveFigure.get(13) instanceof SuperBrzaFigura) {
                if ("CRVENA".equals(sveFigure.get(13).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_CRVENA);
                        Image image = new Image(fileInputStream);
                        figurica14.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else if ("PLAVA".equals(sveFigure.get(13).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_PLAVA);
                        Image image = new Image(fileInputStream);
                        figurica14.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else if ("ZUTA".equals(sveFigure.get(13).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZUTA);
                        Image image = new Image(fileInputStream);
                        figurica14.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else if ("ZELENA".equals(sveFigure.get(13).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZELENA);
                        Image image = new Image(fileInputStream);
                        figurica14.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }

            }//
            if(sveFigure.get(14) instanceof LebdecaFigura){
                if("CRVENA".equals(sveFigure.get(14).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_CRVENA);
                        Image image=new Image(fileInputStream);
                        figurica15.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else  if("PLAVA".equals(sveFigure.get(14).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_PLAVA);
                        Image image=new Image(fileInputStream);
                        figurica15.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZUTA".equals(sveFigure.get(14).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZUTA);
                        Image image=new Image(fileInputStream);
                        figurica15.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZELENA".equals(sveFigure.get(14).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZELENA);
                        Image image=new Image(fileInputStream);
                        figurica15.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }
            }else if(sveFigure.get(14) instanceof ObicnaFigura){
                if("CRVENA".equals(sveFigure.get(14).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_CRVENA);
                        Image image=new Image(fileInputStream);
                        figurica15.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else  if("PLAVA".equals(sveFigure.get(14).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_PLAVA);
                        Image image=new Image(fileInputStream);
                        figurica15.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZUTA".equals(sveFigure.get(14).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZUTA);
                        Image image=new Image(fileInputStream);
                        figurica15.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZELENA".equals(sveFigure.get(14).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZELENA);
                        Image image=new Image(fileInputStream);
                        figurica15.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }
            }else if(sveFigure.get(14) instanceof SuperBrzaFigura) {
                if ("CRVENA".equals(sveFigure.get(14).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_CRVENA);
                        Image image = new Image(fileInputStream);
                        figurica15.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else if ("PLAVA".equals(sveFigure.get(14).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_PLAVA);
                        Image image = new Image(fileInputStream);
                        figurica15.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else if ("ZUTA".equals(sveFigure.get(14).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZUTA);
                        Image image = new Image(fileInputStream);
                        figurica15.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else if ("ZELENA".equals(sveFigure.get(14).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZELENA);
                        Image image = new Image(fileInputStream);
                        figurica15.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }

            }
            //
            if(sveFigure.get(15) instanceof LebdecaFigura){
                if("CRVENA".equals(sveFigure.get(15).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_CRVENA);
                        Image image=new Image(fileInputStream);
                        figurica16.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else  if("PLAVA".equals(sveFigure.get(15).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_PLAVA);
                        Image image=new Image(fileInputStream);
                        figurica16.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZUTA".equals(sveFigure.get(15).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZUTA);
                        Image image=new Image(fileInputStream);
                        figurica16.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZELENA".equals(sveFigure.get(15).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_LETECA_ZELENA);
                        Image image=new Image(fileInputStream);
                        figurica16.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }
            }else if(sveFigure.get(15) instanceof ObicnaFigura){
                if("CRVENA".equals(sveFigure.get(15).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_CRVENA);
                        Image image=new Image(fileInputStream);
                        figurica16.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else  if("PLAVA".equals(sveFigure.get(15).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_PLAVA);
                        Image image=new Image(fileInputStream);
                        figurica16.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZUTA".equals(sveFigure.get(15).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZUTA);
                        Image image=new Image(fileInputStream);
                        figurica16.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }else  if("ZELENA".equals(sveFigure.get(15).getBoja())){
                    try{
                        FileInputStream fileInputStream=new FileInputStream(Slika.SLIKA_OBICNA_ZELENA);
                        Image image=new Image(fileInputStream);
                        figurica16.setImage(image);
                    }catch (Exception e){
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }
            }else if(sveFigure.get(15) instanceof SuperBrzaFigura) {
                if ("CRVENA".equals(sveFigure.get(15).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_CRVENA);
                        Image image = new Image(fileInputStream);
                        figurica16.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else if ("PLAVA".equals(sveFigure.get(15).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_PLAVA);
                        Image image = new Image(fileInputStream);
                        figurica16.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else if ("ZUTA".equals(sveFigure.get(15).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZUTA);
                        Image image = new Image(fileInputStream);
                        figurica16.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                } else if ("ZELENA".equals(sveFigure.get(15).getBoja())) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(Slika.SLIKA_SUPER_BRZA_ZELENA);
                        Image image = new Image(fileInputStream);
                        figurica16.setImage(image);
                    } catch (Exception e) {
                        Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                    }
                }

            }
        }
    }
    }



