import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class RGBToTGA {
	public static int swap(int other){
		other &= 0xFFFF;
		return (other>>8)&0xFF|(other<<8)&0xFF00;
	}
	public static void main(String[] args) throws IOException {
		DataOutputStream dos = new DataOutputStream(System.out);
		dos.writeByte(0);// IDIlen
		dos.writeByte(0); // Palette???
		dos.writeByte(2); // Typ: RGB (24 Bit) unkomprimiert
		dos.writeShort(0);// Palette-Start
		dos.writeShort(0);// Palette-Count
		dos.writeByte(0);// Palette-Stride
		dos.writeShort(0);// O-X
		int width = 960;
		int height = 540;
		dos.writeShort(swap(height));// O-Y

		
		dos.writeShort(swap(width));// width
		dos.writeShort(swap(height));// height

		dos.writeByte(24);// 

		dos.writeByte(32);// Maybe 16
		InputStream in = System.in;
		while(true){
			int d1 =in.read();
			int d2 =in.read();
			int d3 =in.read();
			int d4 =in.read();
			if(d4==-1){
				break;
			}
			dos.write(d4);
			dos.write(d3);
			dos.write(d2);
		}
		dos.flush();
	}
}
