package sim.util.io.stream;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.media.Buffer;
import javax.media.ConfigureCompleteEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.DataSink;
import javax.media.EndOfMediaEvent;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.PrefetchCompleteEvent;
import javax.media.Processor;
import javax.media.ProcessorModel;
import javax.media.RealizeCompleteEvent;
import javax.media.ResourceUnavailableEvent;
import javax.media.Time;
import javax.media.control.TrackControl;
import javax.media.datasink.DataSinkErrorEvent;
import javax.media.datasink.DataSinkEvent;
import javax.media.datasink.DataSinkListener;
import javax.media.datasink.EndOfStreamEvent;
import javax.media.format.JPEGFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.FileTypeDescriptor;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;

import com.sun.media.format.AviVideoFormat;


public class JMFRTPStreamer extends VideoStreamer implements ControllerListener, DataSinkListener{
	
	BufferedImage out;
	int fps;
	
	public JMFRTPStreamer() {
		super();
	}
	
	public JMFRTPStreamer(int fps) {
		super(fps);
	}
	
	public JMFRTPStreamer(Dimension size) {
		super(size);
	}
	
	public JMFRTPStreamer(int fps, Dimension size) {
		super(fps, size);
		
		this.out = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
		this.fps = fps;
	}
	
	public void setupStreamer() throws IOException
	{		
		final JMFRTPStreamer parent = this;
		new Thread() {
			public void run() {
				try {
					MediaLocator locator = new MediaLocator("rtp://192.168.1.144:10000/video");
					MediaLocator stream = new MediaLocator("rtp://192.168.1.112:10000/video");
					
					ImageDataSource ids = new ImageDataSource(size.width, size.height, fps);
					//DataSource clone = javax.media.Manager.createCloneableDataSource(ids);
					
					ids.connect();
			        //clone.connect();
					
					Format[] outputFormat=new Format[1];
					FileTypeDescriptor outputType = new FileTypeDescriptor(FileTypeDescriptor.RAW_RTP);
					outputFormat[0] = new VideoFormat(VideoFormat.JPEG_RTP);
					
					ProcessorModel processorModel = new ProcessorModel(ids, outputFormat, outputType);
					Processor p = Manager.createRealizedProcessor(processorModel);
					
					//processor.realize();
					if (!waitForState(p, p.Configured)) {
						// System.err.println("Failed to realize the processor.");
						p.close();
						p.deallocate();
						return;
					}
					
					TrackControl[] tracks = p.getTrackControls();
					for (int i = 0; i < tracks.length; i++) {
						Format format = tracks[i].getFormat();
						if (tracks[i].isEnabled() && format instanceof VideoFormat) {
							// Found a video track. Try to program it to output
							// JPEG/RTP
							VideoFormat jpegFormat = new VideoFormat(VideoFormat.JPEG_RTP, size, Format.NOT_SPECIFIED, Format.byteArray, fps);
							tracks[i].setFormat(jpegFormat);
							System.out.println("Video Format set: " + tracks[i].getFormat());
						} else
							tracks[i].setEnabled(false);
					}
					
					p.setContentDescriptor(new ContentDescriptor(ContentDescriptor.RAW_RTP));
					

					// Now, we'll need to create a DataSink.
					DataSink dsink;
					if ((dsink = createDataSink(p, stream)) == null) {
						System.err.println("Failed to create a DataSink for the given output MediaLocator: " + locator);
						p.close();
						p.deallocate();
						return;
					}

					dsink.addDataSinkListener(parent);
					fileDone = false;
					
					try {
						p.start();
						dsink.start();
					} catch (IOException e) {
						p.close();
						p.deallocate();
						dsink.close();
						System.err.println("IO error during processing");
						return;
					}

					// Wait for EndOfStream
					
					while(!fileDone) Thread.sleep(100);

					// Cleanup.
					try {
						dsink.close();
					} catch (Exception e) {
					}
					p.removeControllerListener(parent);

					System.err.println("...done processing.");

					p.close();
					
				} catch (Exception e) {
					System.err.println("Unable to start RTP Stream");
				}
				
			}
		}.start();
		
		
	}
	
	@Override
	public void stream() {
		// TODO Auto-generated method stub
		out.getGraphics().drawImage(video, 0, 0,  size.width, size.height, null);
	}
	
	DataSink createDataSink(Processor p, MediaLocator outML) {

		DataSource ds;

		if ((ds = p.getDataOutput()) == null) {
			System.err.println("Something is really wrong: the processor does not have an output DataSource");
			return null;
		}

		DataSink dsink;

		try {
			dsink = Manager.createDataSink(ds, outML);
			dsink.open();
		} catch (Exception e) {
			System.err.println("Cannot create the DataSink: " + e);
			return null;
		}

		return dsink;
	}
	
	Object waitSync = new Object();
	boolean stateTransitionOK = true;

	/**
	 * Block until the processor has transitioned to the given state. Return
	 * false if the transition failed.
	 */
	boolean waitForState(Processor p, int state) {
		synchronized (waitSync) {
			try {
				while (p.getState() < state && stateTransitionOK)
					waitSync.wait();
			} catch (Exception e) {
			}
		}
		return stateTransitionOK;
	}
	
	public void controllerUpdate(ControllerEvent evt) {

		if (evt instanceof ConfigureCompleteEvent || evt instanceof RealizeCompleteEvent
				|| evt instanceof PrefetchCompleteEvent) {
			synchronized (waitSync) {
				stateTransitionOK = true;
				waitSync.notifyAll();
			}
		} else if (evt instanceof ResourceUnavailableEvent) {
			synchronized (waitSync) {
				stateTransitionOK = false;
				waitSync.notifyAll();
			}
		} else if (evt instanceof EndOfMediaEvent) {
			evt.getSourceController().stop();
			evt.getSourceController().close();
		}
	}
	
	Object waitFileSync = new Object();
	boolean fileDone = false;
	boolean fileSuccess = true;
	
	/**
	 * Block until file writing is done.
	 */
	boolean waitForFileDone() {
		synchronized (waitFileSync) {
			try {
				while (!fileDone)
					waitFileSync.wait();
			} catch (Exception e) {
			}
		}
		return fileSuccess;
	}

	/**
	 * Event handler for the file writer.
	 */
	public void dataSinkUpdate(DataSinkEvent evt) {

		if (evt instanceof EndOfStreamEvent) {
			synchronized (waitFileSync) {
				fileDone = true;
				waitFileSync.notifyAll();
			}
		} else if (evt instanceof DataSinkErrorEvent) {
			synchronized (waitFileSync) {
				fileDone = true;
				fileSuccess = false;
				waitFileSync.notifyAll();
			}
		}
	}

	class ImageDataSource extends PullBufferDataSource {

		ImageSourceStream streams[];

		ImageDataSource(int width, int height, int frameRate) {
			streams = new ImageSourceStream[1];
			streams[0] = new ImageSourceStream(width, height, frameRate);
		}

		public void setLocator(MediaLocator source) {}

		public MediaLocator getLocator() { return null;	}

		/**
		 * Content type is of RAW since we are sending buffers of video frames
		 * without a container format.
		 */
		public String getContentType() {
			return ContentDescriptor.RAW;
		}

		public void connect() {
			
		}

		public void disconnect() {}

		public void start() {}

		public void stop() {}

		/**
		 * Return the ImageSourceStreams.
		 */
		public PullBufferStream[] getStreams() {
			return streams;
		}

		/**
		 * We could have derived the duration from the number of frames and
		 * frame rate. But for the purpose of this program, it's not necessary.
		 */
		public Time getDuration() { return DURATION_UNKNOWN; }
		public Object[] getControls() { return new Object[0]; }
		public Object getControl(String type) { return null; }
	}

	/**
	 * The source stream to go along with ImageDataSource.
	 */
	class ImageSourceStream implements PullBufferStream {

		int width, height;
		VideoFormat format;

		int nextImage = 0; // index of the next image to be read.
		boolean ended = false;

		public ImageSourceStream(int width, int height, int frameRate) {
			this.width = width;
			this.height = height;

			format = new JPEGFormat(new Dimension(width, height), Format.NOT_SPECIFIED, Format.byteArray,
					(float) frameRate, 75, JPEGFormat.DEC_422);
			//format = new AviVideoFormat(encoding, size, maxDataLength, dataType, frameRate, planes, bitsPerPixel, imageSize, xPelsPerMeter, yPelsPerMeter, clrUsed, clrImportant, codecHeader)
		}

		/**
		 * We should never need to block assuming data are read from files.
		 */
		public boolean willReadBlock() {
			return false;
		}

		/**
		 * This is called from the Processor to read a frame worth of video
		 * data.
		 */
		public void read(Buffer buf) throws IOException {

			// Open a random access file for the next image.
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();   
			ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
			ImageIO.write(out, "jpeg", ios);

			byte data[] = null;
			
			//System.out.println("Reading File");

			// Check the input buffer type & size.

			byte[] b = baos.toByteArray();
			
			if (buf.getData() instanceof byte[])
				data = (byte[]) buf.getData();

			// Check to see the given buffer is big enough for the frame.
			//if (data == null || data.length < ios.length()) {
			//	data = new byte[(int) ios.length()];
			//	buf.setData(data);
			//}
			
			

			//System.out.println("Going to read fully");
			//ios.readFully(b, 0, (int) b.length);
			//System.out.println("Finished ReadFully");
			
			// System.err.println(" read " + raFile.length() + " bytes.");

			buf.setOffset(0);
			buf.setLength(b.length);
			buf.setFormat(format);
			buf.setFlags(buf.getFlags() | buf.FLAG_KEY_FRAME);
			buf.setData(Arrays.copyOf(b, b.length));

			// Close the random access file.
			ios.close();
		}

		/**
		 * Return the format of each video frame. That will be JPEG.
		 */
		public Format getFormat() {
			return format;
		}

		public ContentDescriptor getContentDescriptor() {
			return new ContentDescriptor(ContentDescriptor.RAW);
		}

		public long getContentLength() {return 0; }
		public boolean endOfStream() { return ended; }
		public Object[] getControls() { return new Object[0]; }
		public Object getControl(String type) {	return null; }
	}
}
