package com.letter;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.net.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.io.*;
import java.util.Arrays;
import javax.sound.sampled.*;
import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SoundTemplate app=new SoundTemplate();
    }
}
class SoundTemplate extends JFrame implements Runnable, AdjustmentListener, ActionListener{
    JToggleButton[][] buttons;
    JScrollPane buttonPane;
    JScrollBar tempoBar;
    JLabel tempoLabel;
    JMenuBar menuBar;
    JMenu file, instrumentMenu;
    JMenuItem save, load;
    //JLabel[] labels = new JLabel[buttons.length];
    JButton stopPlay, clear;
    JFileChooser fileChooser;
    JPanel buttonPanel, labelPanel, tempoPanel, menuPanel;

    boolean playing = false;
    int col = 0;
    int tempo;
    String[] clipNames;
    Clip[] clip;
    Font font = new Font("Times New Roman",Font.PLAIN,10);
    String[] instrumentNames = {"Bell","Piano","Glockenspiel","Marimba","Oboe","Oh_Ah"};
    public SoundTemplate() {
        clip=new Clip[37];
        clipNames = new String[]{"C","B","ASharp","A","GSharp","G","FSharp","F","E","DSharp","D","CSharp"};

        loadClips(0);

        fileChooser = new JFileChooser(System.getProperty("user.dir"));

        instrumentMenu = new JMenu("Instruments");
        for(int i = 0; i<instrumentNames.length; i++){
            JMenuItem item = new JMenuItem(instrumentNames[i]);
            item.addActionListener(this);
            instrumentMenu.add(item);
        }

        setButtons(180);

        menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(1,2));

        stopPlay = new JButton("Play");
        stopPlay.addActionListener(e -> {
            col = 0;
            playing = !playing;
            stopPlay.setText(playing ? "Stop":"Play");
        });

        clear = new JButton("Clear");
        clear.addActionListener(e -> {
            for(JToggleButton[] i : buttons)
                for(JToggleButton j : i)
                    j.setSelected(false);
            reset();
        });

        tempoBar = new JScrollBar(JScrollBar.HORIZONTAL,200,0,50,500);
        tempoBar.addAdjustmentListener(this);
        tempo = tempoBar.getValue();
        tempoLabel = new JLabel(String.format("%s%6s","Tempo: ",tempo));
        tempoPanel = new JPanel(new BorderLayout());
        tempoPanel.add(tempoLabel,BorderLayout.WEST);
        tempoPanel.add(tempoBar,BorderLayout.CENTER);

        menuPanel.add(stopPlay);
        menuPanel.add(clear);


        file = new JMenu("File");
        save = new JMenuItem("Save");
        save.addActionListener(e -> {
            FileFilter filter = new FileNameExtensionFilter("*.txt","txt");
            fileChooser.setFileFilter(filter);
            if(fileChooser.showSaveDialog(null)==JFileChooser.APPROVE_OPTION){
                File file = fileChooser.getSelectedFile();
                try{
                    String output = tempo+"";
                    for(int i = 0; i<buttons[0].length-(tempo+"").length(); i++)
                        output+=" ";
                    output+="\n";
                    for(JToggleButton[] i : buttons) {
                        for (JToggleButton j : i)
                            output += (j.isSelected() ? "x" : "-");
                        output+="\n";
                    }

                    BufferedWriter outputStream = new BufferedWriter(new FileWriter(file.getAbsolutePath()));
                    outputStream.write(output);
                    outputStream.close();
                }
                catch(IOException er){
                    er.printStackTrace();
                }
            }
        });
        load = new JMenuItem("Load");
        load.addActionListener(e -> {
            int val = fileChooser.showOpenDialog(this);
            if(val==JFileChooser.APPROVE_OPTION){
                try{
                    File file = fileChooser.getSelectedFile();
                    BufferedReader input = new BufferedReader(new FileReader(file));
                    String temp = input.readLine();
                    tempo = Integer.parseInt(temp.trim());
                    tempoBar.setValue(tempo);

                    setButtons(temp.length());

                    for(JToggleButton[] i : buttons) {
                        temp = input.readLine();
                        for (int j = 0; j < i.length; j++)
                            if (temp.charAt(j)=='x')
                                i[j].setSelected(true);
                    }


                    this.revalidate();
                }
                catch(Exception er){
                    er.printStackTrace();
                }
            }
        });
        file.add(save);
        file.add(load);

        menuBar = new JMenuBar();
        menuBar.setLayout(new GridLayout(1,2));
        menuBar.add(file);
        menuBar.add(instrumentMenu);
        menuBar.add(menuPanel,BorderLayout.EAST);

        this.add(buttonPane,BorderLayout.CENTER);
        this.add(tempoPanel,BorderLayout.SOUTH);
        this.add(menuBar,BorderLayout.NORTH);

        setSize(1000,800);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Thread timing = new Thread(this);
        timing.start();
    }

    public void run() {
        do {
            try {
                if(!playing) {
                    new Thread().sleep(0);
                }
                else {
                    for (int r = 0; r < clip.length; r++)
                        if (buttons[r][col].isSelected()) {
                            clip[r].start();
                            buttons[r][col].setForeground(Color.YELLOW);
                        }
                    new Thread().sleep(tempo);
                    for (int r = 0; r < clip.length; r++)
                        if (buttons[r][col].isSelected()) {
                            clip[r].stop();
                            clip[r].setFramePosition(0);
                            buttons[r][col].setForeground(Color.BLACK);
                        }
                    col++;
                    if (col == buttons[0].length)
                        col = 0;
                }
            }
            catch(InterruptedException e) {
            }
        }while(true);
    }
    public void reset(){
        col = 0;
        playing = false;
        stopPlay.setText("Play");
    }

    public void adjustmentValueChanged(AdjustmentEvent e) {
        tempo = tempoBar.getValue();
        tempoLabel.setText(String.format("%s%6s","Tempo: ",tempo));
    }

    public void loadClips(int i){
        String initInstrument = "src/"+instrumentNames[i]+"/"+instrumentNames[i]+" - ";
        try {
            for (int x = 0; x < 37; x++) {
                File file = new File(initInstrument+clipNames[x%12]+(x%12==0 ? x/12 : (x/12)+1)+".wav");
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
                clip[x] = AudioSystem.getClip();
                clip[x].open(audioIn);
            }
        } catch (UnsupportedAudioFileException|IOException|LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    public void setButtons(int i){
        buttonPanel = new JPanel();
        buttons =new JToggleButton[37][i];
        buttonPanel.setLayout(new GridLayout(buttons.length,buttons[0].length,2,5));
        for(int y=0;y<buttons.length;y++)
            for(int x=0;x<buttons[0].length;x++) {
                buttons[y][x] = new JToggleButton();
                buttons[y][x].setFont(font);
                buttons[y][x].setText(clipNames[y%12].replaceAll("Sharp","#")+(y%12==0 ? y/12 : (y/12)+1));
                buttons[y][x].setPreferredSize(new Dimension(30,30));
                buttons[y][x].setMargin(new Insets(0,0,0,0));
                buttonPanel.add(buttons[y][x]);
            }
        //this.remove(buttonPane);
        buttonPane = new JScrollPane(buttonPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        this.add(buttonPane,BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent e) {
        for(int i = 0; i< instrumentMenu.getMenuComponents().length; i++)
            if(e.getSource().equals(instrumentMenu.getMenuComponent(i)))
                loadClips(i);
        reset();
    }
}
