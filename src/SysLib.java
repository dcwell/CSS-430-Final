import java.util.*;

/**
 * Edits by Denali Cornwell & Jayden Stipek
 */
public class SysLib {
    public static int exec(String args[]) {
        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE,
                Kernel.EXEC, 0, args);
    }

    public static int join() {
        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE,
                Kernel.WAIT, 0, null);
    }

    public static int boot() {
        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE,
                Kernel.BOOT, 0, null);
    }

    public static int exit() {
        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE,
                Kernel.EXIT, 0, null);
    }

    public static int sleep(int milliseconds) {
        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE,
                Kernel.SLEEP, milliseconds, null);
    }

    public static int disk(int type) {
        return Kernel.interrupt(Kernel.INTERRUPT_DISK,
                type, 0, null);
    }

    public static int cin(StringBuffer s) {
        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE,
                Kernel.READ, 0, s);
    }

    public static int cout(String s) {
        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE,
                Kernel.WRITE, 1, s);
    }

    public static int cerr(String s) {
        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE,
                Kernel.WRITE, 2, s);
    }

    public static int rawread(int blkNumber, byte[] b) {
        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE,
                Kernel.RAWREAD, blkNumber, b);
    }

    public static int rawwrite(int blkNumber, byte[] b) {
        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE,
                Kernel.RAWWRITE, blkNumber, b);
    }

    public static int sync() {
        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE,
                Kernel.SYNC, 0, null);
    }

    public static int cread(int blkNumber, byte[] b) {
        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE,
                Kernel.CREAD, blkNumber, b);
    }

    public static int cwrite(int blkNumber, byte[] b) {
        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE,
                Kernel.CWRITE, blkNumber, b);
    }

    public static int flush() {
        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE,
                Kernel.CFLUSH, 0, null);
    }

    public static int csync() {
        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE,
                Kernel.CSYNC, 0, null);
    }

    /****************************************************
     * Heres The code given to us
     * *************************************************
     */

    public static String[] stringToArgs(String s) {
        StringTokenizer token = new StringTokenizer(s, " ");
        String[] progArgs = new String[token.countTokens()];
        for (int i = 0; token.hasMoreTokens(); i++) {
            progArgs[i] = token.nextToken();
        }
        return progArgs;
    }

    public static void short2bytes(short s, byte[] b, int offset) {
        b[offset] = (byte) (s >> 8);
        b[offset + 1] = (byte) s;
    }

    public static short bytes2short(byte[] b, int offset) {
        short s = 0;
        s += b[offset] & 0xff;
        s <<= 8;
        s += b[offset + 1] & 0xff;
        return s;
    }

    public static void int2bytes(int i, byte[] b, int offset) {
        b[offset] = (byte) (i >> 24);
        b[offset + 1] = (byte) (i >> 16);
        b[offset + 2] = (byte) (i >> 8);
        b[offset + 3] = (byte) i;
    }

    public static int bytes2int(byte[] b, int offset) {
        int n = ((b[offset] & 0xff) << 24) + ((b[offset + 1] & 0xff) << 16) +
                ((b[offset + 2] & 0xff) << 8) + (b[offset + 3] & 0xff);
        return n;
    }

    /****************************************************
     * HERES THE CODE ADDED FOR PROJ 5
     * *************************************************
     */

    /**
     * Close an fd from view of a thread.
     *
     * @param fd Which fd to close.
     */
    public static int close(int fd) {
        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE,
                Kernel.CLOSE, fd, null);
    }

    /**
     * Format blocks for filesystem usage.
     *
     * @param files This is the number of files (blocks) to format on the disk.
     */
    public static int format(int files) {
        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE,
                Kernel.FORMAT, files, null);
    }

    /**
     * Opens a file for filessystem usage. Can either open in read or write modes.
     *
     * @param fileName Name of the file to be opened.
     * @param mode The mode the opened file should be in.
     */
    public static int open(String fileName, String mode) {
        String[] args = new String[]{fileName, mode};
        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE,
                Kernel.OPEN, 0, args);
    }

    /**
     * Read bytes from a file to a byte array.
     *
     * @param fd file descriptor to read from.
     * @param buf the buffer to read into.
     */
    public static int read(int fd, byte[] buf) {
        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE,
                Kernel.READ, fd, buf);
    }

    /**
     * Writes bytes from a byte array to a file descriptor.
     *
     * @param fd File descriptor to write to.
     * @param buf Buffer with bytes to write into fd.
     */
    public static int write(int fd, byte[] buf) {
        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE,
                Kernel.WRITE, fd, buf);
    }

    /**
     * Seek information across a file using a "pointer" that can go forwards and reverse.
     *
     * @param fd The FD that contains the file to seek through.
     * @param offset The offest within the file to seek from.
     * @param whence Where to seek from in the file, back, front, or middle.
     */
    public static int seek(int fd, int offset, int whence) {
        int[] args = new int[]{offset, whence};
        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE,
                Kernel.SEEK, fd, args);
    }

    /**
     * Delete a file in the file system.
     *
     * @param fileName The name of the file to be deleted.
     */
    public static int delete(String fileName) {
        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE,
                Kernel.DELETE, 0, fileName);
    }

    /**
     * Get the size of a file at a file descriptor.
     *
     * @param fd FD for file to get size at.
     */
    public static int fsize(int fd) {
        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE,
                Kernel.SIZE, fd, null);
    }
}
