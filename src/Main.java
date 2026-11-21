import javax.sound.sampled.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import javax.sound.sampled.TargetDataLine;

public class Main {
    private static final Color BG = new Color(13, 16, 25), PANEL = new Color(22, 26, 38), MUTED = new Color(142, 151, 172), WHITE = new Color(239, 242, 249), LIME = new Color(193, 255, 106), PURPLE = new Color(160, 123, 255), CYAN = new Color(83, 224, 221);
    private final JFrame frame = new JFrame("SONORA — sound into form");
    private final JLabel statusLabel = new JLabel("Choose an audio file to begin");
    private final ArtworkPanel artwork = new ArtworkPanel();
    private FrequencyColorBar frequencyColorBar;
    private final JButton saveButton = button("SAVE ARTWORK", LIME, BG);
    private final JButton recordButton = button("●   START RECORDING", BG, WHITE);
    private final JButton stopButton = button("■   STOP", new Color(71, 37, 48), WHITE);
    private final JButton importButton = button("＋   IMPORT AUDIO FILE", BG, WHITE);
    private volatile boolean recording;
    private volatile TargetDataLine microphone;
    private volatile long recordingStarted;
    private AudioFeatures current;

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new Main().launch()); }

    private void launch() {
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1000, 720)); frame.setSize(1120, 790); frame.setLocationRelativeTo(null);
        JPanel root = new JPanel(new BorderLayout(26, 0)); root.setBackground(BG); root.setBorder(new EmptyBorder(28, 36, 30, 36));
        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        JLabel brand = new JLabel("S O N O R A"); brand.setFont(new Font("SansSerif", Font.BOLD, 14)); brand.setForeground(LIME);
        JLabel tagline = new JLabel("A LISTENING MACHINE   /   AUDIO → IMAGE"); tagline.setFont(new Font("SansSerif", Font.PLAIN, 11)); tagline.setForeground(MUTED);
        header.add(brand, BorderLayout.WEST); header.add(tagline, BorderLayout.EAST);
        JPanel left = new JPanel(); left.setOpaque(false); left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS)); left.setPreferredSize(new Dimension(340, 1));
        JLabel title = new JLabel("Hear the shape."); title.setFont(new Font("Serif", Font.PLAIN, 42)); title.setForeground(WHITE); title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel intro = new JLabel("Sing, play, or make a sound. Watch it take shape in real time."); intro.setFont(new Font("SansSerif", Font.PLAIN, 13)); intro.setForeground(MUTED); intro.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel upload = panel(); upload.setLayout(new BoxLayout(upload, BoxLayout.Y_AXIS)); upload.setBorder(new EmptyBorder(22, 20, 20, 20)); upload.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel step = eyebrow("01   /   CAPTURE A SOUND"); step.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel micTitle = new JLabel("Your microphone"); micTitle.setForeground(WHITE); micTitle.setFont(new Font("SansSerif", Font.BOLD, 14)); micTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel formats = new JLabel("Live analysis · 10-second window · WAV / MP3 / M4A / FLAC"); formats.setForeground(MUTED); formats.setFont(new Font("SansSerif", Font.PLAIN, 11)); formats.setAlignmentX(Component.LEFT_ALIGNMENT);
        recordButton.setAlignmentX(Component.LEFT_ALIGNMENT); recordButton.addActionListener(e -> startRecording());
        stopButton.setAlignmentX(Component.LEFT_ALIGNMENT); stopButton.setEnabled(false); stopButton.addActionListener(e -> stopRecording());
        importButton.setAlignmentX(Component.LEFT_ALIGNMENT); importButton.addActionListener(e -> importFile());
        statusLabel.setForeground(MUTED); statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11)); statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        upload.add(step); upload.add(Box.createVerticalStrut(16)); upload.add(new JLabel("Choose an audio file"));
        Component fileTitle = upload.getComponent(upload.getComponentCount()-1); fileTitle.setForeground(WHITE); ((JLabel)fileTitle).setFont(new Font("SansSerif", Font.BOLD, 14));
        upload.add(Box.createVerticalStrut(7)); upload.add(formats); upload.add(Box.createVerticalStrut(16)); upload.add(importButton); upload.add(Box.createVerticalStrut(12)); upload.add(statusLabel);
        saveButton.setAlignmentX(Component.LEFT_ALIGNMENT); saveButton.setEnabled(false); saveButton.addActionListener(e -> saveImage());
        JLabel fileHeading = eyebrow("01   /   MAKE ARTWORK FROM A SONG"); fileHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel liveHeading = eyebrow("02   /   WATCH A LIVE PATTERN GROW"); liveHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(title); left.add(Box.createVerticalStrut(10)); left.add(intro); left.add(Box.createVerticalStrut(27)); left.add(fileHeading); left.add(Box.createVerticalStrut(10)); left.add(upload);
        // Keep file import and microphone capture as distinct, clearly labelled sections.
        JPanel live = panel(); live.setLayout(new BoxLayout(live, BoxLayout.Y_AXIS)); live.setBorder(new EmptyBorder(18, 20, 18, 20)); live.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel liveTitle = new JLabel("Live recording"); liveTitle.setForeground(WHITE); liveTitle.setFont(new Font("SansSerif", Font.BOLD, 14)); liveTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel liveDesc = new JLabel("Each moment adds another petal to the canvas."); liveDesc.setForeground(MUTED); liveDesc.setFont(new Font("SansSerif", Font.PLAIN, 11)); liveDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        recordButton.setAlignmentX(Component.LEFT_ALIGNMENT); stopButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        live.add(liveTitle); live.add(Box.createVerticalStrut(7)); live.add(liveDesc); live.add(Box.createVerticalStrut(15)); live.add(recordButton); live.add(Box.createVerticalStrut(8)); live.add(stopButton);
        left.add(Box.createVerticalStrut(22)); left.add(liveHeading); left.add(Box.createVerticalStrut(10)); left.add(live); left.add(Box.createVerticalStrut(16)); left.add(saveButton);
        JPanel right = new JPanel(new BorderLayout(0, 12)); right.setOpaque(false);
        JPanel artTop = new JPanel(new BorderLayout()); artTop.setOpaque(false); JLabel artLabel = eyebrow("GENERATED CANVAS"); JLabel seed = new JLabel("WAITING FOR SOUND"); seed.setForeground(MUTED); seed.setFont(new Font("Monospaced", Font.PLAIN, 10)); artTop.add(artLabel, BorderLayout.WEST); artTop.add(seed, BorderLayout.EAST);
        right.add(artTop, BorderLayout.NORTH); right.add(artwork, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout()); footer.setOpaque(false); JLabel footerLeft = new JLabel("SONORA   /   EVERY SOUND LEAVES A TRACE"); footerLeft.setForeground(MUTED); footerLeft.setFont(new Font("Monospaced", Font.PLAIN, 10)); JLabel footerRight = new JLabel("LOCAL ANALYSIS"); footerRight.setForeground(LIME); footerRight.setFont(new Font("Monospaced", Font.PLAIN, 10)); footer.add(footerLeft, BorderLayout.WEST); footer.add(footerRight, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH); root.add(left, BorderLayout.WEST); root.add(right, BorderLayout.CENTER); root.add(footer, BorderLayout.SOUTH);
        frame.setContentPane(root); frame.setVisible(true);
    }

    private void startRecording() {
        // Do not start a second microphone capture while one is already running.
        if (recording) return;
        // Clear the previous result so the new recording starts with a blank canvas.
        current = null; artwork.setFeatures(null);
        if (frequencyColorBar != null) frequencyColorBar.setFeatures(null);
        // Update the controls before starting the background microphone thread.
        recording = true; recordingStarted = System.nanoTime(); recordButton.setEnabled(false); importButton.setEnabled(false); stopButton.setEnabled(true); saveButton.setEnabled(false);
        statusLabel.setText("Requesting microphone access…");
        Thread capture = new Thread(this::captureMicrophone, "sonora-microphone"); capture.setDaemon(true); capture.start();
    }

    private void stopRecording() {
        recording = false; stopButton.setEnabled(false); statusLabel.setText("Finishing live analysis…");
        TargetDataLine line = microphone; if (line != null) line.close();
    }

    private void captureMicrophone() {
        TargetDataLine line = null;
        float captureRate = 22050;
        try {
            // Try common microphone sample rates until the computer accepts one.
            for (float rate : new float[]{22050, 44100, 48000}) {
                TargetDataLine candidate = null;
                try { AudioFormat candidateFormat = new AudioFormat(rate, 16, 1, true, false); candidate = (TargetDataLine) AudioSystem.getLine(new DataLine.Info(TargetDataLine.class, candidateFormat)); candidate.open(candidateFormat); line = candidate; captureRate = rate; break; }
                catch (Exception ignored) { if (candidate != null) candidate.close(); }
            }
            if (line == null) throw new LineUnavailableException("No supported microphone format was found");
            microphone = line; line.start();
            SwingUtilities.invokeLater(() -> statusLabel.setText("Recording · artwork updates live"));
            // Keep only the newest ten seconds, converted to the app's standard 22,050 Hz rate.
            byte[] bytes = new byte[4096]; ArrayDeque<Float> rolling = new ArrayDeque<>(); int maxSamples = 22050 * 10; long lastAnalysis = 0; int resampleAccumulator = 0;
            while (recording) {
                int read = line.read(bytes, 0, bytes.length); if (read <= 0) continue;
                synchronized (rolling) {
                    for (int i = 0; i + 1 < read; i += 2) { short sample = (short)((bytes[i + 1] << 8) | (bytes[i] & 255)); resampleAccumulator += 22050; if (resampleAccumulator >= captureRate) { rolling.addLast(sample / 32768f); resampleAccumulator -= captureRate; if (rolling.size() > maxSamples) rolling.removeFirst(); } }
                }
                long now = System.nanoTime();
                // Analyse a snapshot roughly every 0.6 seconds so the artwork updates live.
                if (now - lastAnalysis >= 600_000_000L) {
                    lastAnalysis = now; float[] snapshot;
                    synchronized (rolling) { snapshot = new float[rolling.size()]; int i = 0; for (float v : rolling) snapshot[i++] = v; }
                    if (snapshot.length >= 4096) {
                        try {
                            AudioFeatures features = analyseSamples(snapshot, 22050);
                            SwingUtilities.invokeLater(() -> { current = features; update(features, true); double elapsed=(System.nanoTime()-recordingStarted)/1_000_000_000.0; statusLabel.setText("Recording · " + format(elapsed) + " s · updating live"); saveButton.setEnabled(true); });
                        } catch (Exception e) { SwingUtilities.invokeLater(() -> statusLabel.setText("Listening for more sound…")); }
                    }
                }
            }
        } catch (Exception e) {
            String message = rootMessage(e);
            if (recording) SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "Could not access the microphone. Check that one is connected and that microphone access is allowed.\n\n" + message, "Microphone error", JOptionPane.ERROR_MESSAGE));
        } finally {
            if (line != null) { line.close(); } microphone = null;
            recording = false;
            SwingUtilities.invokeLater(() -> { recordButton.setEnabled(true); importButton.setEnabled(true); stopButton.setEnabled(false); if (current != null) { saveButton.setEnabled(true); statusLabel.setText("Recording stopped · latest artwork is ready"); } else { statusLabel.setText("Recording stopped · no audio captured"); } });
        }
    }

    private void importFile() {
        JFileChooser chooser=new JFileChooser(); chooser.setDialogTitle("Import an audio file");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Audio files (WAV, AIFF, AU, MP3, M4A, FLAC)","wav","wave","aiff","aif","au","mp3","m4a","flac"));
        if(chooser.showOpenDialog(frame)!=JFileChooser.APPROVE_OPTION)return;
        File file=chooser.getSelectedFile();recordButton.setEnabled(false);importButton.setEnabled(false);saveButton.setEnabled(false);statusLabel.setText("Analysing "+file.getName()+"…");
        // Decode and analyse in the background so the window remains responsive.
        new SwingWorker<AudioFeatures,Void>() {
            protected AudioFeatures doInBackground() throws Exception { return analyseFile(file); }
            protected void done() {
                recordButton.setEnabled(true);importButton.setEnabled(true);
                try { current=get();update(current, false);saveButton.setEnabled(true);statusLabel.setText("File analysed · "+file.getName()); }
                catch(Exception ex){String message=rootMessage(ex);statusLabel.setText("Could not analyse the file");JOptionPane.showMessageDialog(frame,"Unable to analyse this audio file. Check its format and try again.\n\n"+message,"Audio error",JOptionPane.ERROR_MESSAGE);}
            }
        }.execute();
    }

    private void update(AudioFeatures f, boolean isLive) {
        // Live results add a new layer; an imported song starts a fresh design.
        if (isLive) artwork.addLiveFeatures(f);
        else artwork.setFeatures(f);
        // Keep the sidebar legend in the same song-specific colour range as the artwork.
        if (frequencyColorBar != null) frequencyColorBar.setFeatures(f);
    }
    private void saveImage() { if (current == null) return; JFileChooser c = new JFileChooser(); c.setSelectedFile(new File("sonora-artwork.png")); if (c.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) try { BufferedImage img = artwork.renderImage(1200); ImageIO.write(img, "png", c.getSelectedFile()); statusLabel.setText("Artwork saved · " + c.getSelectedFile().getName()); } catch (IOException ex) { JOptionPane.showMessageDialog(frame, "Could not save the image.", "Save error", JOptionPane.ERROR_MESSAGE); } }

    private static AudioFeatures analyseFile(File file) throws Exception {
        String name=file.getName().toLowerCase(Locale.ROOT);
        if(name.endsWith(".mp3")||name.endsWith(".m4a")||name.endsWith(".flac")){
            // Java does not decode these formats by itself. FFmpeg converts them to a temporary WAV.
            File wav=File.createTempFile("sonora-import-", ".wav"),log=File.createTempFile("sonora-ffmpeg-", ".log");
            try{
                Process process;
                try{process=new ProcessBuilder("ffmpeg","-nostdin","-hide_banner","-loglevel","error","-y","-i",file.getAbsolutePath(),"-map","0:a:0","-vn","-ac","1","-ar","22050","-c:a","pcm_s16le",wav.getAbsolutePath()).redirectError(log).start();}
                catch(IOException e){throw new IOException("FFmpeg is required for MP3, M4A, and FLAC. Install it, add 'ffmpeg' to PATH, and restart Sonora.",e);}
                int exit=process.waitFor();
                if(exit!=0||wav.length()==0){String detail=new String(java.nio.file.Files.readAllBytes(log.toPath()),java.nio.charset.StandardCharsets.UTF_8).trim();throw new IOException("FFmpeg could not decode this audio file."+(detail.isEmpty()?"":" "+detail));}
                return analyseFile(wav);
            }finally{wav.delete();log.delete();}
        }
        try(AudioInputStream source=AudioSystem.getAudioInputStream(file)){
            // Convert the source audio to uncompressed 16-bit PCM samples.
            AudioFormat sf=source.getFormat();AudioFormat pcmFormat=new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,sf.getSampleRate(),16,sf.getChannels(),sf.getChannels()*2,sf.getSampleRate(),false);
            try(AudioInputStream pcm=AudioSystem.getAudioInputStream(pcmFormat,source);ByteArrayOutputStream bytes=new ByteArrayOutputStream()){
                byte[] block=new byte[32768];int n;while((n=pcm.read(block))!=-1)bytes.write(block,0,n);byte[] data=bytes.toByteArray();
                int channels=pcmFormat.getChannels(),frames=data.length/(channels*2);float sourceRate=pcmFormat.getSampleRate();
                // Convert stereo or multi-channel audio to mono and resample it to 22,050 Hz.
                int capacity=(int)Math.ceil(frames*22050.0/sourceRate);float[] samples=new float[capacity];int accumulator=0,out=0;
                for(int frame=0;frame<frames;frame++){
                    double sum=0;for(int c=0;c<channels;c++){int p=(frame*channels+c)*2;short value=(short)((data[p+1]<<8)|(data[p]&255));sum+=value/32768.0;}
                    accumulator+=22050;if(accumulator>=sourceRate){samples[out++]=(float)(sum/channels);accumulator-=sourceRate;}
                }
                return analyseSamples(Arrays.copyOf(samples,out),22050);
            }
        }
    }

    private static AudioFeatures analyseSamples(float[] x, int sampleRate) throws IOException {
        if (x.length < 4096) throw new IOException("Waiting for more microphone audio");
        // Measure overall volume and the loudest sample.
        double rms=0,peak=0; for(float v:x){rms+=v*v;peak=Math.max(peak,Math.abs(v));} rms=Math.sqrt(rms/x.length);
        // Break audio into overlapping windows and measure the strength of each frequency.
        int win=2048,hop=512,bins=win/2;double[] spectrum=new double[bins],window=new double[win];
        for(int i=0;i<win;i++)window[i]=0.5-0.5*Math.cos(2*Math.PI*i/(win-1));
        double[] re=new double[win],im=new double[win];
        for(int start=0;start+win<=x.length;start+=hop){
            for(int i=0;i<win;i++){re[i]=x[start+i]*window[i];im[i]=0;}
            fft(re,im);for(int k=1;k<bins;k++)spectrum[k]+=Math.hypot(re[k],im[k]);
        }
        // Use the spectrum to estimate brightness (centroid) and the strongest musical note.
        double total=0,weighted=0,max=0;int maxBin=1;for(int k=1;k<bins;k++){total+=spectrum[k];weighted+=spectrum[k]*k*sampleRate/(double)win;if(spectrum[k]>max){max=spectrum[k];maxBin=k;}}
        double centroid=total==0?0:weighted/total;String note=noteName(maxBin*sampleRate/(double)win);
        int envHop=256,envLen=(x.length+envHop-1)/envHop;double[] env=new double[envLen];
        for(int i=0;i<envLen;i++){double q=0;int end=Math.min(x.length,(i+1)*envHop);for(int j=i*envHop;j<end;j++)q+=x[j]*x[j];env[i]=Math.sqrt(q/Math.max(1,end-i*envHop));}
        // Track volume in short chunks; repeating peaks in this envelope suggest the tempo.
        double mean=Arrays.stream(env).average().orElse(0);for(int i=0;i<env.length;i++)env[i]-=mean;
        double envRate=sampleRate/(double)envHop,best=-1;int bestLag=0;
        for(int lag=(int)(envRate*60/180);lag<Math.min(env.length/2,(int)(envRate*60/55));lag++){double corr=0;for(int i=0;i+lag<env.length;i++)corr+=env[i]*env[i+lag];if(corr>best){best=corr;bestLag=lag;}}
        double bpm=bestLag==0?0:60*envRate/bestLag;if(bpm>0&&bpm<75)bpm*=2;
        // Group frequencies into 64 bands used to draw the artwork.
        double[] bands=new double[64];for(int b=0;b<64;b++){int lo=1+(int)(Math.pow(b/64.0,2)*(bins-1)),hi=1+(int)(Math.pow((b+1)/64.0,2)*(bins-1));hi=Math.max(lo+1,hi);for(int k=lo;k<Math.min(hi,bins);k++)bands[b]+=spectrum[k];}
        return new AudioFeatures(x,bands,rms,peak,centroid,bpm,note,x.length/(double)sampleRate,Math.max(0,peak-rms),sampleRate);
    }

    private static void fft(double[] re,double[] im){
        // Rearrange the samples, then combine pairs of frequencies in progressively larger groups.
        int n=re.length;for(int i=1,j=0;i<n;i++){int bit=n>>1;for(; (j&bit)!=0;bit>>=1)j^=bit;j^=bit;if(i<j){double t=re[i];re[i]=re[j];re[j]=t;t=im[i];im[i]=im[j];im[j]=t;}}
        for(int len=2;len<=n;len<<=1){double angle=-2*Math.PI/len,wlenR=Math.cos(angle),wlenI=Math.sin(angle);for(int i=0;i<n;i+=len){double wr=1,wi=0;for(int j=0;j<len/2;j++){int u=i+j,v=i+j+len/2;double vr=re[v]*wr-im[v]*wi,vi=re[v]*wi+im[v]*wr;re[v]=re[u]-vr;im[v]=im[u]-vi;re[u]+=vr;im[u]+=vi;double next=wr*wlenR-wi*wlenI;wi=wr*wlenI+wi*wlenR;wr=next;}}}
    }

    private static String noteName(double f){if(f<20)return "—";String[] names={"C","C♯","D","D♯","E","F","F♯","G","G♯","A","A♯","B"};int m=(int)Math.round(69+12*Math.log(f/440)/Math.log(2));return names[Math.floorMod(m,12)]+(m/12-1);}
    private static String format(double d){return new DecimalFormat("0.0").format(d);}
    private static String rootMessage(Throwable t){return t.getMessage()==null?t.getClass().getSimpleName():t.getMessage();}
    private static JPanel panel(){JPanel p=new JPanel();p.setBackground(PANEL);p.setBorder(BorderFactory.createLineBorder(new Color(42,47,62)));return p;}
    private static JLabel eyebrow(String s){JLabel l=new JLabel(s);l.setForeground(MUTED);l.setFont(new Font("Monospaced",Font.BOLD,10));return l;}
    private static JButton button(String text,Color bg,Color fg){JButton b=new JButton(text);b.setFont(new Font("SansSerif",Font.BOLD,11));b.setForeground(fg);b.setBackground(bg);b.setFocusPainted(false);b.setBorder(new EmptyBorder(13,16,13,16));b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));return b;}
    private static void addKey(JPanel p,Color c,String title,String desc){JPanel row=new JPanel(new BorderLayout(10,0));row.setOpaque(false);JPanel dot=new JPanel();dot.setBackground(c);dot.setPreferredSize(new Dimension(5,5));JLabel t=new JLabel(title);t.setForeground(WHITE);t.setFont(new Font("SansSerif",Font.BOLD,10));JLabel d=new JLabel(desc);d.setForeground(MUTED);d.setFont(new Font("SansSerif",Font.PLAIN,10));row.add(dot,BorderLayout.WEST);row.add(t,BorderLayout.CENTER);row.add(d,BorderLayout.EAST);p.add(row);}

    // Build a compact legend that labels the lowest and highest frequencies used by the artwork.
    private JPanel createFrequencyScale() {
        JPanel scale = new JPanel();
        scale.setOpaque(false);
        scale.setLayout(new BoxLayout(scale, BoxLayout.Y_AXIS));

        frequencyColorBar = new FrequencyColorBar();
        FrequencyColorBar colorBar = frequencyColorBar;
        colorBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel labels = new JPanel(new BorderLayout());
        labels.setOpaque(false);
        // Audio is resampled to 22,050 Hz and analysed in 2,048-sample windows.
        // The first frequency band starts near 11 Hz and the highest reaches the 11,025 Hz Nyquist limit.
        JLabel lowFrequency = new JLabel("11 Hz");
        JLabel highFrequency = new JLabel("11.0 kHz");
        lowFrequency.setForeground(MUTED);
        highFrequency.setForeground(MUTED);
        lowFrequency.setFont(new Font("Monospaced", Font.PLAIN, 10));
        highFrequency.setFont(new Font("Monospaced", Font.PLAIN, 10));
        labels.add(lowFrequency, BorderLayout.WEST);
        labels.add(highFrequency, BorderLayout.EAST);

        scale.add(colorBar);
        scale.add(Box.createVerticalStrut(5));
        scale.add(labels);
        return scale;
    }

    // Convert a position from low to high frequency into the same colour used on the wave.
    private static Color frequencyColor(double position, float baseHue) {
        // Keep a song's key colour as the anchor, with a warm pink/red/purple sweep.
        float[] offsets = {-0.055f, 0f, 0.075f};
        int segment = Math.min(1, (int)(position * 2));
        float t = (float)(position * 2 - segment);
        float hue = (baseHue + offsets[segment] * (1-t) + offsets[segment+1] * t + 1f) % 1f;
        return Color.getHSBColor(hue, 0.76f, 0.98f);
    }

    // Shift the frequency gradient around the colour wheel for each song, and brighten stronger bands.
    private static Color frequencyColor(double position, float baseHue, float brightness) {
        float hue = (baseHue + (float) position * 0.20f + 1f) % 1f;
        return Color.getHSBColor(hue, 0.78f, brightness);
    }

    // Paint the low-to-high frequency gradient shown in section 03.
    private static class FrequencyColorBar extends JPanel {
        private float baseHue;

        FrequencyColorBar() {
            setOpaque(false);
            setPreferredSize(new Dimension(280, 16));
            setMinimumSize(new Dimension(240, 16));
            // Let BoxLayout stretch the bar across the full width available in the sidebar.
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
        }

        // Update the legend to use the current song's dominant frequency colour.
        void setFeatures(AudioFeatures features) {
            baseHue = features == null ? 0 : (float) (Math.abs(features.centroid) % 4000 / 4000.0);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Colour each thin vertical strip to make one continuous-looking gradient.
            for (int x = 0; x < getWidth(); x++) {
                double position = x / (double) Math.max(1, getWidth() - 1);
                g.setColor(frequencyColor(position, baseHue));
                g.drawLine(x, 1, x, getHeight() - 2);
            }

            g.setColor(new Color(230, 234, 246, 100));
            g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            g.dispose();
        }
    }

    private static class AudioFeatures { final float[] samples; final double[] bands; final double rms,peak,centroid,bpm,duration,dynamic; final String note; final int rate; AudioFeatures(float[] s,double[] b,double r,double p,double c,double tempo,String nt,double d,double dyn,int sr){samples=s;bands=b;rms=r;peak=p;centroid=c;bpm=tempo;note=nt;duration=d;dynamic=dyn;rate=sr;} }
    private class ArtworkPanel extends JPanel {
        private AudioFeatures f;
        private BufferedImage accumulatedWaves;
        private final ArrayDeque<AudioFeatures> liveHistory = new ArrayDeque<>();
        private static final int MAX_LIVE_UPDATES = 7;
        private static final int ARTWORK_SIZE = 1200;
        ArtworkPanel(){setBackground(new Color(9,12,20));setBorder(BorderFactory.createLineBorder(new Color(42,47,62)));}

        // Replace any earlier image with a fresh artwork for this analysis result.
        void setFeatures(AudioFeatures v){
            f = v;
            liveHistory.clear();
            accumulatedWaves = new BufferedImage(ARTWORK_SIZE, ARTWORK_SIZE, BufferedImage.TYPE_INT_ARGB);
            if (v != null) addWaveToImage(v, 0.9f);
            repaint();
        }

        // Add the latest live analysis on top of the waves already drawn.
        void addLiveFeatures(AudioFeatures v){
            f = v;
            liveHistory.addLast(v);
            while (liveHistory.size() > MAX_LIVE_UPDATES) liveHistory.removeFirst();
            accumulatedWaves = new BufferedImage(ARTWORK_SIZE, ARTWORK_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D historyGraphics = accumulatedWaves.createGraphics();
            historyGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (AudioFeatures update : liveHistory) drawWaves(historyGraphics, ARTWORK_SIZE, ARTWORK_SIZE, update, 1f);
            historyGraphics.dispose();
            repaint();
        }

        // Draw one audio snapshot into the persistent image at a low opacity for live layering.
        private void addWaveToImage(AudioFeatures features, float opacity) {
            Graphics2D imageGraphics = accumulatedWaves.createGraphics();
            imageGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawWaves(imageGraphics, ARTWORK_SIZE, ARTWORK_SIZE, features, opacity);
            imageGraphics.dispose();
        }

        // Draw the current artwork into an image so it can be saved as a PNG.
        BufferedImage renderImage(int size){BufferedImage im=new BufferedImage(size,size,BufferedImage.TYPE_INT_RGB);Graphics2D g=im.createGraphics();paintArtwork(g,size,size);g.dispose();return im;}

        // Draw the saved wave image scaled to the current app window.
        protected void paintComponent(Graphics graphics){super.paintComponent(graphics);Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);paintArtwork(g,getWidth(),getHeight());g.dispose();}
        private void paintArtwork(Graphics2D g, int w, int h) {
            // Clear the canvas before drawing the new artwork.
            g.setColor(new Color(9, 12, 20));
            g.fillRect(0, 0, w, h);

            // Show a simple guide until sound has been analysed.
            if (f == null) {
                g.setColor(new Color(35, 41, 57));
                for (int ring = 1; ring <= 8; ring++) {
                    int radius = ring * Math.min(w, h) / 12;
                    g.drawOval(w / 2 - radius, h / 2 - radius, radius * 2, radius * 2);
                }
                g.setColor(MUTED);
                g.setFont(new Font("Monospaced", Font.PLAIN, 12));
                center(g, "YOUR SOUND WILL LIVE HERE", w / 2, h / 2);
                return;
            }

            // Use the smaller canvas dimension so the circular wave fits in either window shape.
            int size = Math.min(w, h);
            int centerX = w / 2;
            int centerY = h / 2;

            // Show every snapshot recorded so far, preserving the shapes from earlier moments.
            if (accumulatedWaves != null) {
                g.drawImage(accumulatedWaves, 0, 0, w, h, null);
            }

            // Keep the readout current even though earlier live waves remain visible.
            g.setColor(new Color(230, 234, 246, 150));
            g.setFont(new Font("Monospaced", Font.PLAIN, Math.max(9, size / 80)));
            g.drawString(f.note + "   /   " + Math.round(f.bpm) + " BPM", 18, h - 20);
        }

        // Create one set of waves from a single audio analysis result.
        private void drawWaves(Graphics2D g, int w, int h, AudioFeatures audio, float opacity) {
            int size = Math.min(w, h);
            int centerX = w / 2;
            int centerY = h / 2;

            // The spectral centroid chooses this song's base hue; frequency bands shift colour along the wave.
            int key = 0;
            try { String[] notes = {"C","C♯","D","D♯","E","F","F♯","G","G♯","A","A♯","B"}; String pitch = audio.note.replaceAll("-?\\d", ""); for (int i=0;i<notes.length;i++) if(notes[i].equals(pitch)) key=i; } catch(Exception ignored) {}
            // Pitch class anchors the palette; different songs also get distinct petal structures.
            float baseHue = (float)(0.88 - key / 12.0 * 0.62); if(baseHue<0)baseHue+=1;
            double maxBand = Arrays.stream(audio.bands).max().orElse(1);
            int points = 720;
            int layers = 8;

            // Draw outer layers first so the inner waves remain visible on top.
            for (int layer = layers - 1; layer >= 0; layer--) {
                Path2D wave = new Path2D.Double();
                double[] pointX = new double[points];
                double[] pointY = new double[points];

                // Keep the wave layers nested around the centre.
                double baseRadius = size * (0.035 + layer * 0.034);
                double layerAmplitude = size * (0.085 + layer * 0.009);

                // Trace a full circle and use each audio section to set the local wave height.
                for (int point = 0; point < points; point++) {
                    double angle = 2 * Math.PI * point / points;

                    // Find the matching short section of the audio samples.
                    int sampleStart = (int)((long)point * audio.samples.length / points);
                    int sampleEnd = Math.min(audio.samples.length, sampleStart + Math.max(1, audio.samples.length / points));
                    double localEnergy = 0;
                    for (int sample = sampleStart; sample < sampleEnd; sample++) {
                        localEnergy += audio.samples[sample] * audio.samples[sample];
                    }

                    // RMS gives the section's amplitude; square root makes quiet variations easier to see.
                    double localLevel = Math.sqrt(localEnergy / Math.max(1, sampleEnd - sampleStart));
                    localLevel = Math.min(1.0, Math.sqrt(localLevel) * 3.4);
                    // Repeating radial lobes create petals; the audio envelope controls their height.
                    double petals = 0.58 + 0.42 * Math.pow(Math.max(0, Math.cos(angle * (5 + layer % 4) + layer * 0.37)), 2);
                    double radius = baseRadius + layerAmplitude * (0.18 + localLevel * 1.8) * petals;
                    double x = centerX + Math.cos(angle) * radius;
                    double y = centerY + Math.sin(angle) * radius;
                    pointX[point] = x;
                    pointY[point] = y;

                    // Connect points around the circle, then close the outline back to its start.
                    if (point == 0) wave.moveTo(x, y);
                    else wave.lineTo(x, y);
                }
                wave.closePath();

                // Tint each layer with the song's base colour so the whole image changes between songs.
                float layerHue = (baseHue + layer * 0.075f) % 1f;
                Color layerColor = Color.getHSBColor(layerHue, 0.75f, 0.95f);
                int fillAlpha = Math.max(0, Math.min(255, Math.round(35 * opacity)));
                int lineAlpha = Math.max(0, Math.min(255, Math.round(180 * opacity)));
                g.setColor(new Color(layerColor.getRed(), layerColor.getGreen(), layerColor.getBlue(), fillAlpha));
                g.fill(wave);

                // Divide the outline into small pieces so each frequency band can have its own colour.
                g.setStroke(new BasicStroke(Math.max(1f, size / 450f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int point = 0; point < points; point++) {
                    int nextPoint = (point + 1) % points;
                    int bandIndex = (point * audio.bands.length / points + layer * 7) % audio.bands.length;
                    double bandStrength = maxBand == 0 ? 0 : audio.bands[bandIndex] / maxBand;

                    // Shift each frequency band from this song's base hue, matching the sidebar gradient.
                    double frequencyPosition = bandIndex / (double) (audio.bands.length - 1);
                    float brightness = (float) Math.min(1, 0.65 + bandStrength * 0.35);
                    Color segmentColor = frequencyColor(frequencyPosition, baseHue, brightness);
                    g.setColor(new Color(segmentColor.getRed(), segmentColor.getGreen(),
                            segmentColor.getBlue(), lineAlpha));
                    g.draw(new Line2D.Double(pointX[point], pointY[point],
                            pointX[nextPoint], pointY[nextPoint]));
                }
            }
        }

        private void center(Graphics2D g,String s,int x,int y){FontMetrics fm=g.getFontMetrics();g.drawString(s,x-fm.stringWidth(s)/2,y);}
    }
}
