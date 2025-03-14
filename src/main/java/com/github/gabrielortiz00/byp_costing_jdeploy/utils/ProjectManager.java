package com.github.gabrielortiz00.byp_costing_jdeploy.utils;

import com.github.gabrielortiz00.byp_costing_jdeploy.ResultsModel;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectManager {

    private static final String PROJECTS_DIRECTORY = "projects";


    public static boolean saveProject(ResultsModel project) {
        File directory = new File(PROJECTS_DIRECTORY);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                System.out.println("Failed to create projects directory");
                return false;
            }
        }

        //create serializable file
        String filename = project.getProjectName().replaceAll("\\s+", "_")
                + "_" + System.currentTimeMillis() + ".ser";
        File file = new File(directory, filename);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(project);
            return true;
        } catch (IOException e) {
            System.out.println("Error saving project");
            e.printStackTrace();
            return false;
        }
    }


    public static List<ResultsModel> loadProjects() {
        List<ResultsModel> projects = new ArrayList<>();
        File directory = new File(PROJECTS_DIRECTORY);

        if (!directory.exists()) {
            return projects;
        }

        File[] files = directory.listFiles((dir, name) -> name.endsWith(".ser"));
        if (files == null) {
            return projects;
        }

        for (File file : files) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Object obj = ois.readObject();
                if (obj instanceof ResultsModel) {
                    projects.add((ResultsModel) obj);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error loading project from " + file.getName());
            }
        }

        return projects;
    }


    public static boolean deleteProject(ResultsModel project) {
        File directory = new File(PROJECTS_DIRECTORY);
        if (!directory.exists()) {
            return false;
        }

        //find the matching file
        File[] files = directory.listFiles((dir, name) -> name.startsWith(project.getProjectName().replaceAll("\\s+", "_")));

        if (files == null || files.length == 0) {
            return false;
        }

        //delete file
        boolean success = true;
        for (File file : files) {
            if (!file.delete()) {
                success = false;
            }
        }

        return success;
    }
}