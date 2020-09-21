package se.hig.dvg306.modul3app.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DemModel {
    public enum AttributeOrder {V, VN, VNT, VT}

    ;

    private AttributeOrder attributeOrder;

    //Data to be generated
    private List<Float> vertices = new ArrayList<>();
    private List<Float> normals = new ArrayList<>();
    private List<Float> textureCords = new ArrayList<>();

    int nCols;
    int nRows;
    float cellSize;

    float xllCenter;
    float yllCenter;

    float noDataVal;

    String[][] graph;

    float maxHeight = 0;

    float maxXval;
    float maxYval;

    public DemModel(List<String> input) {


        // get the metadata
        nCols = Integer.parseInt(input.get(0).replace("ncols ", ""));
        nRows = Integer.parseInt(input.get(1).replace("nrows ", ""));
        xllCenter = Float.parseFloat(input.get(2).replace("xllcenter ", ""));
        yllCenter = Float.parseFloat(input.get(3).replace("yllcenter ", ""));
        cellSize = Float.parseFloat(input.get(4).replace("cellsize ", ""));
        noDataVal = Float.parseFloat(input.get(5).replace("nodata_value ", ""));

        //these maxvalues are used to calulate the texturecoordiantes later
         maxXval = (cellSize * nCols) + 25;
         maxYval = (cellSize * nRows) + 25;


        //remove the metadata from the string list
        for (int i = 0; i < 6; i++) {
            input.remove(0);
        }

        // this is a two dimensionall list which will make it easier to find neighboring nodes and its height values
        graph = new String[nRows][nCols];

        //fill the "graph" with the appropiate values
        for (int i = 0; i < input.size(); i++) {
            //one line contains one entire row, height values are seperated with a number of whitespaces
            // using the regular expression \\s will use all number of whitespaces as a decimeter in the split function
            String[] line = input.get(i).split("\\s+");
            graph[i] = line;
        }

        // now we iterate over the entire graph and these floats[] will be used to generate two triangles,
        // all these triangles will be added together and form the model.
        float[] current = new float[4];
        float[] above = new float[4];
        float[] aboveLeft = new float[4];
        float[] right = new float[4];

        for (int i = 2; i < nRows - 2; i += 1) {
            for (int j = 2; j < nCols - 2; j += 1) {




                //node above
                float x = ((j - 1) * cellSize) + cellSize / 2;
                float y = (i * cellSize) + cellSize / 2;
                float z = Float.parseFloat(graph[i][j]);

                //store the node
                above[0] = x;
                above[1] = y;
                above[2] = z;
                above[3] = 1;

                //add the node to the verticeData list
                addToVerticeList(above);


                //current node
                x = ((j) * cellSize) + cellSize / 2;
                y = (i * cellSize) + cellSize / 2;
                z = Float.parseFloat(graph[i][j + 1]);

                current[0] = x;
                current[1] = y;
                current[2] = z;
                current[3] = 1;

                addToVerticeList(current);

                //get max height
                if(z > maxHeight)
                    maxHeight = z;


                //node to the right
                x = ((j) * cellSize) + cellSize / 2;
                y = ((i + 1) * cellSize) + cellSize / 2;
                z = Float.parseFloat(graph[i + 1][j + 1]);

                right[0] = x;
                right[1] = y;
                right[2] = z;
                right[3] = 1;

                addToVerticeList(right);


                addToVerticeList(above);


                //node to the above left
                x = ((j - 1) * cellSize) + cellSize / 2;
                y = ((i - 1) * cellSize) + cellSize / 2;
                z = Float.parseFloat(graph[i - 1][j]);


                aboveLeft[0] = x;
                aboveLeft[1] = y;
                aboveLeft[2] = z;
                aboveLeft[3] = 1;

                addToVerticeList(aboveLeft);


                addToVerticeList(current);


                // calculate norms and texture coords and add them to their lists in the same order as the nodes above were created
                calcAboveNormals(above, current, aboveLeft, i, j);
                addToTextureCoord(above[0],above[1]);
                calcCurrentNormals(current, above, right, i, j);
                addToTextureCoord(current[0],current[1]);
                calcRightNormals(right, current, i, j);
                addToTextureCoord(right[0],right[1]);
                calcAboveNormals(above, current, aboveLeft, i, j);
                addToTextureCoord(above[0],above[1]);
                calcAboveLeftNormals(aboveLeft, above, i, j);
                addToTextureCoord(aboveLeft[0],aboveLeft[1]);
                calcCurrentNormals(current, above, right, i, j);
                addToTextureCoord(current[0],current[1]);


            }
        }



    }


    void addToVerticeList(float[] node) {
        for (int i = 0; i < 4; i++) {
            this.vertices.add(node[i]);
        }
    }
    void addToNormalList(float[] n) {
        for (int i = 0; i < 4; i++) {
            this.normals.add(n[i]);
        }
    }
    void addToTextureCoord(float x, float y){
        textureCords.add(x/maxXval);
        textureCords.add(y/maxYval);
    }


    // All these calcNormals functions will calculate the normals for the four nodes added in one iteration.
    // this is done by using the crossporduct on the 4 neighbouring nodes and then interpolating between those to get one "average" normal
    private void calcAboveNormals(float[] current, float[] below, float[] left, int i, int j) {

        float[] above = new float[4];
        //node above
        float x = ((j - 2) * cellSize) + cellSize / 2;
        float y = (i * cellSize) + cellSize / 2;
        float z = Float.parseFloat(graph[i][j - 1]);

        above[0] = x;
        above[1] = y;
        above[2] = z;
        above[3] = 1;

        float[] right = new float[4];
        x = ((j - 1) * cellSize) + cellSize / 2;
        y = ((i + 1) * cellSize) + cellSize / 2;
        z = Float.parseFloat(graph[i + 1][j]);

        right[0] = x;
        right[1] = y;
        right[2] = z;
        right[3] = 1;

        float[] n1 = calcNorm(current,above,right);
        float[] n2 = calcNorm(current,right,below);
        float[] n3 = calcNorm(current,below,left);
        float[] n4 = calcNorm(current,left,above);

        addToNormalList(interpolateNorm(n1,n2,n3,n4));



    }


    private void calcCurrentNormals(float[] current, float[] above, float[] right, int i, int j) {
        float[] left = new float[4];
        float x = ((j) * cellSize) + cellSize / 2;
        float y = ((i - 1) * cellSize) + cellSize / 2;
        float z = Float.parseFloat(graph[i - 1][j + 1]);

        left[0] = x;
        left[1] = y;
        left[2] = z;
        left[3] = 1;


        float[] below = new float[4];
        x = ((j + 1) * cellSize) + cellSize / 2;
        y = (i * cellSize) + cellSize / 2;
        z = Float.parseFloat(graph[i][j + 2]);

        below[0] = x;
        below[1] = y;
        below[2] = z;
        below[3] = 1;

        float[] n1 = calcNorm(current,above,right);
        float[] n2 = calcNorm(current,right,below);
        float[] n3 = calcNorm(current,below,left);
        float[] n4 = calcNorm(current,left,above);

        addToNormalList(interpolateNorm(n1,n2,n3,n4));



    }

    private void calcRightNormals(float[] current, float[] left, int i, int j) {
        float[] right = new float[4];
        float x = ((j) * cellSize) + cellSize / 2;
        float y = ((i + 2) * cellSize) + cellSize / 2;
        float z = Float.parseFloat(graph[i + 2][j + 1]);


        right[0] = x;
        right[1] = y;
        right[2] = z;
        right[3] = 1;

        float[] below = new float[4];
        x = ((j + 1) * cellSize) + cellSize / 2;
        y = ((i + 1) * cellSize) + cellSize / 2;
        z = Float.parseFloat(graph[i + 1][j + 2]);

        below[0] = x;
        below[1] = y;
        below[2] = z;
        below[3] = 1;

        float[] above = new float[4];
        x = ((j - 1) * cellSize) + cellSize / 2;
        y = ((i + 1) * cellSize) + cellSize / 2;
        z = Float.parseFloat(graph[i + 1][j]);

        above[0] = x;
        above[1] = y;
        above[2] = z;
        above[3] = 1;

        float[] n1 = calcNorm(current,above,right);
        float[] n2 = calcNorm(current,right,below);
        float[] n3 = calcNorm(current,below,left);
        float[] n4 = calcNorm(current,left,above);

        addToNormalList(interpolateNorm(n1,n2,n3,n4));

    }

    private void calcAboveLeftNormals(float[] current, float[] right, int i, int j) {
        float[] above = new float[4];
        float x = ((j - 2) * cellSize) + cellSize / 2;
        float y = ((i - 1) * cellSize) + cellSize / 2;
        float z = Float.parseFloat(graph[i - 1][j - 1]);


        above[0] = x;
        above[1] = y;
        above[2] = z;
        above[3] = 1;


        float[] left = new float[4];
        x = ((j-1) * cellSize) + cellSize / 2;
        y = ((i - 2) * cellSize) + cellSize / 2;
        z = Float.parseFloat(graph[i - 2][j]);

        left[0] = x;
        left[1] = y;
        left[2] = z;
        left[3] = 1;

        float[] below = new float[4];
        x = ((j) * cellSize) + cellSize / 2;
        y = ((i - 1) * cellSize) + cellSize / 2;
        z = Float.parseFloat(graph[i - 1][j + 1]);

        below[0] = x;
        below[1] = y;
        below[2] = z;
        below[3] = 1;

        float[] n1 = calcNorm(current,above,right);
        float[] n2 = calcNorm(current,right,below);
        float[] n3 = calcNorm(current,below,left);
        float[] n4 = calcNorm(current,left,above);

        addToNormalList(interpolateNorm(n1,n2,n3,n4));

}

    // this function does the vector algebra and uses the cross product to calculate the normals
    float[] calcNorm(float [] current, float[] a, float[] b ){
        //cross product for (a - current) X (b - current) gives normal
        float[] res1 = new float[4];
        res1[0] = a[0] - current[0];
        res1[1] = a[1] - current[1];
        res1[2] = a[2] - current[2];
        res1[3] = 0;


        float[] res2 = new float[4];
        res2[0] = b[0] - current[0];
        res2[1] = b[1] - current[1];
        res2[2] = b[2] - current[2];
        res2[3] = 0;

        float[] crossProduct = new float[4];
        crossProduct[0] = res1[1]*res2[2] - res1[2]*res2[1];
        crossProduct[1] = res1[2]*res2[0] - res1[0]*res2[2];
        crossProduct[2] = res1[0]*res2[1] - res1[1]*res2[0];
        crossProduct[3] = 0;

        return crossProduct;
    }

    // Interpolates the four normal vectors calculated above
    float[] interpolateNorm(float[] n1,float[] n2,float[] n3,float[] n4){
        float[] res = new float[4];
        float[] num;
        num = vectorAdd(n1,n2,n3,n4);

        float dem  = vectorAbs(n1) + vectorAbs(n2) +vectorAbs(n3) +vectorAbs(n4);

        for (int i = 0; i < 4; i++) {
            res[i] = num[i] / dem;
        }


        return res;
    }

    //returns vector length used for interpolation
    float vectorAbs(float[] v){
        return (float) Math.sqrt(Math.pow(v[0],2) + Math.pow(v[1],2) + Math.pow(v[2],2));
    }

    float[] vectorAdd(float[] v1, float[] v2, float[] v3, float[] v4){
        float[] res = new float[4];
        for (int i = 0; i < 4; i++) {
            res[i] = v1[i] + v2[i]+ v3[i] + v4[i];
        }
        return res;
    }


    public float[] getVerticeData() {
        float[] arr = new float[vertices.size()];
        int index = 0;
        for (final Float value : vertices) {
            arr[index++] = value;
        }
        return arr;
    }

    //return the calculated data
    public float[] getNormalData() {
        float[] arr = new float[normals.size()];
        int index = 0;
        for (final Float value : normals) {
            arr[index++] = value;
        }
        return arr;
    }
    public float[] getTextCoords() {
        float[] arr = new float[textureCords.size()];
        int index = 0;
        for (final Float value : textureCords) {
            arr[index++] = value;
        }
        return arr;
    }

    public float getMaxHeight(){
        return maxHeight;
    }

}