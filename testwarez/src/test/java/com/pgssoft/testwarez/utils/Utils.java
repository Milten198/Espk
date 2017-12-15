package com.pgssoft.testwarez.utils;

import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static junit.framework.Assert.assertNotNull;

/**
 * Created by dpodolak on 01.07.16.
 */
public class Utils {

    public static String readJson(String fileName) {

        File jsonFile = new File(getAssetDir(), fileName);

        StringBuilder jsonBuilder = new StringBuilder();


        String line;

        BufferedReader br = null;
        try {
            assertNotNull(jsonFile);


            br = new BufferedReader(new FileReader(jsonFile));

            while ((line = br.readLine()) != null) {
                jsonBuilder.append(line);
            }

        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return jsonBuilder.toString();


    }

    public static String readGalleryJson(String fileName, int galleryId) {

        File gallerydir = new File(getAssetDir(), String.format("Gallery_%d", galleryId));
        File jsonFile = new File(gallerydir, fileName);

        StringBuilder jsonBuilder = new StringBuilder();


        String line;

        BufferedReader br = null;
        try {
            assertNotNull(jsonFile);


            br = new BufferedReader(new FileReader(jsonFile));

            while ((line = br.readLine()) != null) {
                jsonBuilder.append(line);
            }

        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return jsonBuilder.toString();
    }

    public static String readJson(String fileName, int conferenceId) {

        File conferenceDir = new File(getAssetDir(), String.format("Conference_%d", conferenceId));
        File jsonFile = new File(conferenceDir, fileName);

        if (!jsonFile.exists()) {
            return null;
        }
        StringBuilder jsonBuilder = new StringBuilder();


        String line;

        BufferedReader br = null;
        try {
            assertNotNull(jsonFile);


            br = new BufferedReader(new FileReader(jsonFile));

            while ((line = br.readLine()) != null) {
                jsonBuilder.append(line);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return jsonBuilder.toString();


    }


    @NonNull
    public static File getAssetDir() {
        File assets = new File(new File("").getAbsolutePath(), "/src/test/assets/");
        if (!assets.exists()) {
            assets.mkdirs();
        }

        return assets;
    }

    public static void saveFile(String s, String fileName) {
        if (s == null) {
            return;
        }
        File jsonFile = new File(Utils.getAssetDir(), fileName);

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(jsonFile));
            bw.write(s);
            bw.flush();
            System.out.println("save BEFile: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void saveFile(String s, String fileName, int conferenceId) {
        if (s == null) {
            return;
        }

        File conferenceDir = new File(Utils.getAssetDir(), String.format("Conference_%d", conferenceId));

        if (!conferenceDir.exists()) {
            conferenceDir.mkdirs();
        }

        File jsonFile = new File(conferenceDir, fileName);

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(jsonFile));
            bw.write(s);
            bw.flush();
            System.out.println("save BEFile: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void saveGalleryFile(String s, String fileName, int galleryId) {
        File gallerydir = new File(Utils.getAssetDir(), String.format("Gallery_%d", galleryId));

        if (!gallerydir.exists()) {
            gallerydir.mkdirs();
        }
        File jsonFile = new File(gallerydir, fileName);

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(jsonFile));
            bw.write(s);
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
