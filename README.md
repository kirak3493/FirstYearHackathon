# Sonora

Sonora can analyse either live microphone audio or an imported audio file, then turn the measured sound into generative artwork. It is a Java Swing desktop project.

## Run

Open the project in IntelliJ IDEA and run `Main.main()` from `src/Main.java` (JDK 8 or newer).

- Press **Start Recording** to analyse microphone audio. The artwork and readout update about twice per second from a rolling 10-second window. Press **Stop** to keep the latest image.
- Press **Import Audio File** to analyse WAV, AIFF, AU, MP3, M4A, or FLAC. MP3, M4A, and FLAC require FFmpeg installed and available as `ffmpeg` on your PATH.
- Use **Save Artwork** to export a 1200 × 1200 PNG.

Sonora estimates tempo, dominant pitch class, spectral centroid, average amplitude, dynamics, and frequency-band energy. Pitch is estimated from the strongest frequency bin, so it is a tonal hint rather than polyphonic transcription. Tempo is estimated from periodicity in the amplitude envelope.
