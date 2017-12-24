import java.awt.EventQueue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.swing.JOptionPane;

import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;


import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;



import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.text.AbstractDocument;
import java.awt.Color;



@SuppressWarnings("serial")
public class GUI extends JFrame {
	
	// Constants
	private String startPath = System.getProperty("user.dir");
	private final String propertyName = "Keysound Maker Config.properties";
	private final String version = "v20171221";
	private final int NOTE_ON = 0x90;
	private final int NOTE_OFF = 0x80;
	private int sampleRate = 16000;
	private int channelVolume = 100;
	private int resolution = 480;
	private float divisionType = Sequence.PPQ;
	private int bitDepth = 16;
	private int channelMode = 2; // Stereo
	private int channel = 0;
	private final String[] NOTE_NAMES = { "C", "C#", "D", "D#", "E", "F", "F#","G", "G#", "A", "A#", "B" };
	// http://www.electronics.dit.ie/staff/tscarff/Music_technology/midi/midi_note_numbers_for_octaves.htm
	
	// Variables
	private String outputPath = startPath;
	private int instrument = 0;
	private double BPM = 120;
	private double length = 0.25;
	private int volume = 127;
	private int key = 50;
	private int octave = 6;
	private String pitch = "C";
	
	private Sequencer sequencer;
	private Track track;
	
	// GUI
	private JPanel contentPane;
	private JTextField tf_Output;
	private JTextField tf_BPM;
	private JTextField tf_length;
	private JTextField tf_Instrument;
	private DoubleFilter dfilter = new DoubleFilter();
	private JSpinner spinner;
	private JComboBox<String> comboBox;
	private JSlider sl_Volume;
	private JTextField stdout;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI frame = new GUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	

	/**
	 * Create the frame.
	 */
	public GUI() {
		readFromProperty(startPath);
		setTitle("Keysound Maker by DH " + version );
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 767, 390);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lbl_Output = new JLabel("Output");
		lbl_Output.setBounds(57, 14, 56, 16);
		contentPane.add(lbl_Output);
		
		tf_Output = new JTextField();
		tf_Output.setEditable(false);
		tf_Output.setBounds(123, 11, 483, 22);
		contentPane.add(tf_Output);
		tf_Output.setColumns(10);
		tf_Output.setText(outputPath);
		JButton btn_Output = new JButton("Browse");
		btn_Output.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(null);
			    if (returnVal == JFileChooser.APPROVE_OPTION) {
			            outputPath = fc.getSelectedFile().getPath();
			            tf_Output.setText(outputPath);
			    } 
			}
		});
		btn_Output.setBounds(616, 10, 97, 25);
		contentPane.add(btn_Output);
		
		JButton btn_Play = new JButton("Play");
		btn_Play.setFont(new Font("Tahoma", Font.BOLD, 24));
		btn_Play.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateSequence();
				writeToProperty(startPath);
				try {
					MidiUtils.stopSequence(sequencer);
					MidiUtils.playSequence(sequencer);
				} catch (MidiUnavailableException | InvalidMidiDataException ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(null,ex.getMessage());
				}
			}
		}); 
		btn_Play.setBounds(238, 238, 110, 50);
		contentPane.add(btn_Play);
		
		JLabel lbl_Instrument = new JLabel("Instrument");
		lbl_Instrument.setBounds(57, 59, 73, 14);
		contentPane.add(lbl_Instrument);
		
		JLabel lbl_BPM = new JLabel("BPM");
		lbl_BPM.setBounds(57, 103, 46, 14);
		contentPane.add(lbl_BPM);
		
		JLabel lbl_Length = new JLabel("Length");
		lbl_Length.setToolTipText("");
		lbl_Length.setBounds(263, 103, 46, 14);
		contentPane.add(lbl_Length);
		
		JButton btn_Create = new JButton("Create");
		btn_Create.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateSequence();
				writeToProperty(startPath);
				createWAV();
			}
		});
		btn_Create.setFont(new Font("Tahoma", Font.BOLD, 24));
		btn_Create.setBounds(368, 238, 115, 50);
		contentPane.add(btn_Create);
		
		JLabel lbl_Volume = new JLabel("Volume");
		lbl_Volume.setBounds(57, 191, 46, 14);
		contentPane.add(lbl_Volume);
		
		tf_BPM = new JTextField();
		tf_BPM.setToolTipText("Beats per Minute");
		tf_BPM.setText(""+BPM);
		AbstractDocument document = (AbstractDocument) tf_BPM.getDocument();
        document.setDocumentFilter(dfilter);
		tf_BPM.setBounds(123, 100, 86, 20);
		contentPane.add(tf_BPM);
		tf_BPM.setColumns(10);
	
		tf_length = new JTextField();
		tf_length.setText(""+length);
		AbstractDocument document2 = (AbstractDocument) tf_length.getDocument();
        document2.setDocumentFilter(dfilter);
		tf_length.setToolTipText("Length of note in beats\r\n1/8 beat = 0.125\r\n1/4 beat = 0.25\r\n1/2 beat = 0.5\r\n1 beat = 1");
		tf_length.setBounds(343, 100, 86, 20);
		tf_length.setColumns(10);
		contentPane.add(tf_length);

		tf_Instrument = new JTextField();
		tf_Instrument.setToolTipText("Refer to https://en.wikipedia.org/wiki/General_MIDI#Parameter_interpretations");
		tf_Instrument.setEditable(false);
		tf_Instrument.setBounds(123, 56, 483, 20);
		contentPane.add(tf_Instrument);
		tf_Instrument.setColumns(10);
		setInstrumentName();
		
		JButton btn_Instrument = new JButton("Select");
		btn_Instrument.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				changeInstrument();
			}
		});
		btn_Instrument.setBounds(616, 55, 97, 25);
		contentPane.add(btn_Instrument);
		
		sl_Volume = new JSlider();
		sl_Volume.setToolTipText("0-127");
		sl_Volume.setFont(new Font("Tahoma", Font.BOLD, 11));
		sl_Volume.setValue(100);
		sl_Volume.setPaintLabels(true);
		sl_Volume.setMajorTickSpacing(10);
		sl_Volume.setPaintTicks(true);
		sl_Volume.setMinorTickSpacing(1);
		sl_Volume.setMaximum(127);
		sl_Volume.setBounds(123, 178, 590, 39);
		sl_Volume.setValue(volume);
		contentPane.add(sl_Volume);
		
		JLabel lbl_Octave = new JLabel("Octave");
		lbl_Octave.setBounds(263, 144, 46, 14);
		contentPane.add(lbl_Octave);
		
		spinner = new JSpinner();
		spinner.setToolTipText("0 to 10");
		spinner.setModel(new SpinnerNumberModel(5, 0, 10, 1));
		spinner.setBounds(343, 141, 29, 20);
		spinner.setValue(octave);
		contentPane.add(spinner);
		
		JLabel lbl_Pitch = new JLabel("Pitch");
		lbl_Pitch.setBounds(57, 144, 46, 14);
		contentPane.add(lbl_Pitch);
		
		comboBox = new JComboBox(NOTE_NAMES);
		comboBox.setToolTipText("Refer to http://www.electronics.dit.ie/staff/tscarff/Music_technology/midi/midi_note_numbers_for_octaves.htm");
		comboBox.setBounds(123, 140, 86, 27);
		comboBox.setSelectedItem(pitch);
		contentPane.add(comboBox);
		
		JLabel lblBeats = new JLabel("beat(s)");
		lblBeats.setForeground(Color.BLUE);
		lblBeats.setBounds(439, 103, 44, 14);
		contentPane.add(lblBeats);
		
		stdout = new JTextField();
		stdout.setForeground(Color.MAGENTA);
		stdout.setFont(new Font("Tahoma", Font.BOLD, 20));
		stdout.setEditable(false);
		stdout.setBounds(57, 297, 656, 43);
		contentPane.add(stdout);
		stdout.setColumns(10);
	}
	
	private void changeInstrument() {
		new WindowInstrument(this);
	}
	
	private void updateSequence() {
		BPM = Double.parseDouble( tf_BPM.getText());
		length = Double.parseDouble( tf_length.getText());
		octave = (int) spinner.getValue();
		pitch = comboBox.getSelectedItem().toString();
		key = octave * 12 + comboBox.getSelectedIndex();
		volume = sl_Volume.getValue();
		
		try {
			int tick = 0;
			sequencer = MidiSystem.getSequencer();
			Sequence seq = new Sequence(divisionType, resolution);
			sequencer.setSequence(seq);
			track = seq.createTrack();
		
			// set instrument
			ShortMessage instrumentChange = new ShortMessage();
			instrumentChange.setMessage(ShortMessage.PROGRAM_CHANGE, channel,getInstrument(), tick);
			track.add(new MidiEvent(instrumentChange, tick));
		
			// set channel volume
			ShortMessage channelVolumeChange = new ShortMessage();
			channelVolumeChange.setMessage(ShortMessage.CONTROL_CHANGE,channel, 7, channelVolume);
			track.add(new MidiEvent(channelVolumeChange, tick));
		
		
			// set tempo, us per tick
			long tempo = Utils.BPMToMidiTempo(BPM);
			MetaMessage mm = new MetaMessage();
			byte[] data = MidiUtils.tempoToDataBytes(tempo);
			mm.setMessage(81, data, 3);
			track.add(new MidiEvent(mm, 0));
				
				
			// add note
			int startT = 0;
			int endT = (int) (length * resolution / 0.25);
				
			// start
			ShortMessage msg = new ShortMessage();
			msg.setMessage(NOTE_ON + channel, key, volume);
			track.add(new MidiEvent(msg, startT));
				
			// end
			ShortMessage msg2 = new ShortMessage();
			msg2.setMessage(NOTE_OFF + channel, key, 0);
			track.add(new MidiEvent(msg2, endT));
				
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null,ex.getMessage());
			ex.printStackTrace();
		}

	}
	
	
	private void createWAV() {
		try {
			int tick = (int) (length * resolution / 0.25);
			double duration = (length/0.25) * Utils.BPMToMidiTempo(BPM) / 1000000;
			duration ++;
			MidiToWavRenderer w = new MidiToWavRenderer(sampleRate, bitDepth,channelMode);
			String filename =  getInstrument() + "_" + pitch + octave + "_" + (int) BPM + "_" + tick + ".wav";
			w.createWavFile(sequencer.getSequence(), new File(outputPath + "\\"+filename), duration);
			try {
				MidiUtils.stopSequence(sequencer);
				MidiUtils.playSequence(sequencer);
			} catch (MidiUnavailableException | InvalidMidiDataException ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(null,ex.getMessage());
			}
			stdout.setText(filename + " is created !!!");
			
		} catch (Exception ex){
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null,ex.getMessage());
		}
	}

	 private void readFromProperty(String path) {
			Properties prop = new Properties();
			InputStream input = null;

			try {
				String propertyPath = path + "\\" + propertyName;
				File f = new File(propertyPath);
	        	if (!f.exists()){
	        		f.createNewFile();
	        	}
				
				input = new FileInputStream(propertyPath);
				prop.load(input);
				if (prop.getProperty("Path")!=null){
					outputPath = prop.getProperty("Path");
				} else {
					outputPath = startPath;
				}
				
				if (prop.getProperty("Instrument")!=null){
					instrument = Integer.parseInt(prop.getProperty("Instrument"));
				} else {
					instrument = 0;
				}
				
				if (prop.getProperty("BPM")!=null){
					BPM = Double.parseDouble(prop.getProperty("BPM"));
				} else {
					BPM = 120;
				}
				
				if (prop.getProperty("Length")!=null){
					length = Double.parseDouble(prop.getProperty("Length"));
				} else {
					length = 0.25; // 1/4 beat
				}
				
				if (prop.getProperty("Volume")!=null){
					volume = Integer.parseInt(prop.getProperty("Volume"));
				} else {
					volume = 1;
				}
				
				if (prop.getProperty("Octave")!=null){
					octave = Integer.parseInt(prop.getProperty("Octave"));
				} else {
					octave = 6;
				}

				input.close();
				
				

			} catch (IOException e) {
				JOptionPane.showMessageDialog(null,e.getMessage());
			}

		}
	
	private void writeToProperty(String path) {
		Properties prop = new Properties();
		OutputStream output = null;
		try {
			String propertyPath = path + "\\" + propertyName;
			File f = new File(propertyPath);
			if (!f.exists()){
				f.createNewFile();
			}
			FileInputStream input = new FileInputStream(propertyPath);
			prop.load(input);
			prop.setProperty("Path",outputPath);
			prop.setProperty("Instrument",""+getInstrument());
			prop.setProperty("BPM",""+BPM);
			prop.setProperty("Length",""+length);
			prop.setProperty("Volume",""+volume);
			prop.setProperty("Octave",""+octave);
			input.close();
			// save properties to project root folder
			output = new FileOutputStream(propertyPath);
			prop.store(output, null);
			output.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,e.getMessage());
		}
	}



	public int getInstrument() {
		return instrument;
	}



	public void setInstrument(int instrument) {
		this.instrument = instrument;
	}
	
	public void setInstrumentName() {
		tf_Instrument.setText(MidiUtils.getInstrumentName(0, instrument));
	}
}
