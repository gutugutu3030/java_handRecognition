package imageTranslater;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * javaとprocessingとopencvのための画像オブジェクト変換用クラス
 * ビルド・パスにopencv-xxx.jar（openCV）とcore.jar(Processing)を追加しておいてください．
 *
 * @version 1.1
 */
public class ImageTranslater {
	/**
	 * インスタンス化した時にPAppletを保存しておく．
	 */
	PApplet pa;

	/**
	 * インスタンス化すると短い名前で使用可能です．
	 */
	public ImageTranslater(PApplet pa) {
		this.pa = pa;
	}

	/**
	 * PImage型をBufferedImage型（TYPE_INT_RGBまたはTYPE_INT_ARGB）に変換します
	 *
	 * @param img1 変換したいPImage型
	 * @return 変換したBufferedImage型
	 */
	public static BufferedImage PImageToBufferedImageRGB(PImage img1) {
		int icm = (img1.format == PImage.ARGB) ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
		boolean ifHSB = (img1.format == PImage.HSB);
		BufferedImage img2 = new BufferedImage(img1.width, img1.height, icm);
		img1.loadPixels();
		for (int i = 0; i < img1.width; i++) {
			for (int j = 0; j < img1.height; j++) {
				int color = img1.pixels[i + j * img1.width];
				if (ifHSB) {
					color = Color.HSBtoRGB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF);
				}
				img2.setRGB(i, j, color);
			}
		}
		return img2;
	}

	/**
	 * BufferedImage型をPImage型（RGB）に変換します
	 * @param pa PApplet
	 * @param img1 変換したいBufferedImage型
	 * @return 変換したPImage型
	 */
	public static PImage BufferedImageToPImageRGB(PApplet pa, BufferedImage img1) {
		PImage img2 = pa.createImage(img1.getWidth(), img1.getHeight(), pa.RGB);
		for (int i = 0; i < img2.width; i++) {
			for (int j = 0; j < img2.height; j++) {
				img2.pixels[i + j * img2.width] = img1.getRGB(i, j);
			}
		}
		img2.updatePixels();
		return img2;
	}


	/**
	 * Mat型をBufferedImage型（TYPE_BYTE_GRAYまたはTYPE_3BYTE_BGR）に変換します
	 * @param mat 変換したいMat型
	 * @return 変換したBufferedImage
	 */
	public static BufferedImage MatToBufferedImageBGR(Mat mat) {
		int dataSize = mat.cols() * mat.rows() * (int) mat.elemSize();
		byte data[] = new byte[dataSize];
		mat.get(0, 0, data);
		int type;
		if (mat.channels() == 1) {
			type = BufferedImage.TYPE_BYTE_GRAY;
		} else {
			type = BufferedImage.TYPE_3BYTE_BGR;
			for (int i = 0; i < dataSize; i += 3) {
				byte blue = data[i + 0];
				data[i + 0] = data[i + 2];
				data[i + 2] = blue;
			}
		}

		BufferedImage img = new BufferedImage(mat.cols(), mat.rows(), type);
		img.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);
		return img;
	}

	/**
	 * Mat型をPImage型（RGB）に変換します
	 * @param pa PApplet
	 * @param mat 変換したいMat型
	 * @return 変換したPImage型
	 */
	public static PImage MatToPImageRGB(PApplet pa, Mat mat) {
		PImage img = pa.createImage(mat.cols(), mat.rows(), pa.RGB);
		if (mat.channels() == 1) {
			for(int i=0;i<mat.cols();i++){
				for(int j=0;j<mat.rows();j++){
					int c=(int)mat.get(j,i)[0];
					img.pixels[i+j*img.width]=(c<<16)|(c<<8)|c;
				}
			}
		}else{
			for(int i=0;i<mat.cols();i++){
				for(int j=0;j<mat.rows();j++){
					img.pixels[i+j*img.width]=(int)mat.get(j,i)[0]|((int)mat.get(j,i)[1]<<8)|((int)mat.get(j,i)[2]<<16);
				}
			}
		}
		img.updatePixels();
		return img;
	}

	/**
	 * PImage型をMat型（CV_8UC3）に変換します
	 * @param img 変換したいPImage型
	 * @return 変換したMat型
	 */
	public static Mat PImageToMat(PImage img) {
		Mat mat = new Mat(img.height, img.width, CvType.CV_8UC3);
		int dataSize = mat.cols() * mat.rows() * (int) mat.elemSize();
		byte data[] = new byte[dataSize];
		img.loadPixels();
		int index = 0;
		for (int i = 0; i < dataSize; i += 3) {
			int color = img.pixels[index++];
			data[i + 2] = false ? 0 : (byte) ((color >> 16) & 0xFF);
			data[i + 1] = false ? 0 : (byte) ((color >> 8) & 0xFF);
			data[i] = (byte) (color & 0xFF);
		}
		mat.put(0, 0, data);
		return mat;
	}

	/**
	 * PImage型をMat型（CV_8UC3）に変換します
	 * @param img 変換したいPImage型
	 * @return 変換したMat型
	 */
	public static Mat PImageToMatMask(PImage img) {
		Mat mat = new Mat(img.height, img.width, CvType.CV_8U);
		int dataSize = mat.cols() * mat.rows() * (int) mat.elemSize();
		byte data[] = new byte[dataSize];
		img.loadPixels();
		int index = 0;
		for (int i = 0; i < dataSize; i ++) {
			if((img.pixels[index++]&0x00FFFFFF)!=0);{
				data[i] = 1;
			}
		}
		mat.put(0, 0, data);
		return mat;
	}


	/**
	 * BufferedImage型（TYPE_3BYTE_RGB）をMat型（CV_8UC3）に変換します
	 * @param image 変換したいBufferedImage型
	 * @return 変換したMat型
	 */
	public static Mat BufferedImageToMat(BufferedImage image) {
		byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.out.println("bufferedimage:" + data.length);
		Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
		mat.put(0, 0, data);
		return mat;
	}

	public BufferedImage PI2BI(PImage img1) {
		return PImageToBufferedImageRGB(img1);
	}

	public PImage BI2PI(BufferedImage img1) {
		return BufferedImageToPImageRGB(this.pa, img1);
	}

	public BufferedImage Mat2BI(Mat mat) {
		return MatToBufferedImageBGR(mat);
	}

	public PImage Mat2PI(Mat mat) {
		return MatToPImageRGB(this.pa, mat);
	}

	public Mat PI2Mat(PImage img) {
		Mat mat = new Mat(img.height, img.width, CvType.CV_8UC3);
		int dataSize = mat.cols() * mat.rows() * (int) mat.elemSize();
		byte data[] = new byte[dataSize];
		img.loadPixels();
		int index = 0;
		for (int i = 0; i < dataSize; i += 3) {
			int color = img.pixels[index++];
			data[i + 2] = false ? 0 : (byte) ((color >> 16) & 0xFF);
			data[i + 1] = false ? 0 : (byte) ((color >> 8) & 0xFF);
			data[i] = (byte) (color & 0xFF);
		}
		mat.put(0, 0, data);
		return mat;
	}

	public Mat BI2Mat(BufferedImage image) {
		byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.out.println("bufferedimage:" + data.length);
		Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
		mat.put(0, 0, data);
		return mat;
	}

}
