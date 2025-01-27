public class Main {

    public static void printArray(int[] array){
        System.out.print("[");
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i]+" ");
        }
        System.out.print("]");
    }
    public static void printlnArray(int[] array){
        printArray(array);
        System.out.println();
    }

    public static int[] sort(int[] array) {
        int[] sortedArray = new int[array.length];
        boolean[] inserted = new boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            int min_i = -1;
            for (int j = 0; j < array.length; j++) {
                if (!inserted[j] && (min_i ==-1 || array[j] < array[min_i])) {
                    min_i = j;
                }
            }
            sortedArray[i] = array[min_i];
            inserted[min_i] = true;
        }
        return sortedArray;
    }


    public static void main(String[] args) {
        int[] array = {5, 0, 1, 3, 2, 7, 3, 9, 5, 10, 0};
        int[] sortedArray = sort(array);

        printArray(array);
        System.out.print(" -> ");
        printlnArray(sortedArray);
    }
}