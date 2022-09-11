package projects;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import projects.entity.Project;
import projects.exception.DbException;
import projects.service.ProjectService;

public class ProjectsApp {

	private Scanner scanner = new Scanner(System.in);
	private ProjectService projectService = new ProjectService();
	private Project curProject;

	// @formatter:off
	private List<String> operations = List.of(
		"1) Add a project",
		"2) List projects",
		"3) Select a project",
		"4) Update project details",
		"5) Delete a project"
	);
	// @formatter:on

	public static void main(String[] args) {
		new ProjectsApp().processUserSelections();
	}

	private void processUserSelections() {
		boolean done = false;

		while (!done) {
			try {
				int selection = getUserSelection();

				switch (selection) {
				case -1:
					done = exitMenu();
					break;

				case 1:
					createProject();
					break;

				case 2:
					listProjects();
					break;

				case 3:
					selectProject();
					break;
					
				case 4:
					updateProjectDetails();
					break;
					
				case 5:
					deleteProject();
					break;

				default:
					System.out.println("\n" + selection + " is not a valid selection. Try again.\n");
					break;
				}

			} catch (Exception e) {
				System.out.println("\nError: " + e + "\n");
			}
		}

	}

	private void deleteProject() {
		listProjects();
		Integer projectId = getIntInput("Enter the ID of the project to be deleted");
		
		if (Objects.nonNull(projectId)) {
			
			if (Objects.nonNull(curProject) && curProject.getProjectId().equals(projectId)) {
				curProject = null;
			}
			
			System.out.println("Project ID=" + projectId + ": " + projectService.fetchProjectById(projectId).getProjectName() + " has been deleted.");
			projectService.deleteRecipe(projectId);
			
		} else {
			throw new DbException("Deletion cancelled.\n");
		}
		
	}

	private void updateProjectDetails() {
		if (Objects.isNull(curProject)) {
			System.out.println("\nPlease select a project.\n");
			return;
		}
		
		System.out.println("\n==========UPDATING PROJECT==========");
		String projectName = getStringInput("Enter the new project name [Currently: " + curProject.getProjectName() + "]");
		BigDecimal estHours = getDecimalInput("Enter the estimated hours [Currently: " + curProject.getEstimatedHours() + "]");
		BigDecimal actHours = getDecimalInput("Enter the actual hours [Currently: " + curProject.getActualHours() + "]");
		Integer difficulty = validDifficulty("Enter the project difficulty (1-5) [Currently: " + curProject.getDifficulty() + "]");
		String projectNotes = getStringInput("Enter any notes about the project [Currently: " + curProject.getNotes() + "]");

		Project project = new Project();
		
		project.setProjectName(Objects.isNull(projectName) ? curProject.getProjectName() : projectName);
		project.setEstimatedHours(Objects.isNull(estHours) ? curProject.getEstimatedHours() : estHours);
		project.setActualHours(Objects.isNull(actHours) ? curProject.getActualHours() : actHours);
		project.setDifficulty(Objects.isNull(difficulty) ? curProject.getDifficulty() : difficulty);
		project.setNotes(Objects.isNull(projectNotes) ? curProject.getNotes() : projectNotes);
		project.setProjectId(curProject.getProjectId());
		
		projectService.modifyProjectDetails(project);
		curProject = projectService.fetchProjectById(curProject.getProjectId());
		
	}

	private void selectProject() {
		System.out.println("\n==========SELECT A PROJECT==========");
		listProjects();
		Integer projectId = getIntInput("Enter a project ID to select a project");

		curProject = null;
		curProject = projectService.fetchProjectById(projectId);
		
		System.out.println();

		if (Objects.isNull(curProject)) {
			System.out.println("Error: Invalid project ID.");
		}

	}

	private void listProjects() {
		List<Project> projects = projectService.fetchAllProjects();

		System.out.println("\n==============PROJECTS=============");
		projects.forEach(
				project -> System.out.println("\t" + project.getProjectId() + ": " + project.getProjectName()));
		System.out.print("\n");

	}

	private void createProject() {
		System.out.println("\n==========CREATING NEW PROJECT==========");
		String projectName = getStringInput("\tEnter the project name");
		BigDecimal estimatedHours = getDecimalInput("\tEnter the estimated hours");
		BigDecimal actualHours = getDecimalInput("\tEnter the actual hours");
		Integer difficulty = validDifficulty("\tEnter the project difficulty (1-5)");
		String notes = getStringInput("\tEnter the project notes");

		Project project = new Project();

		project.setProjectName(projectName);
		project.setEstimatedHours(estimatedHours);
		project.setActualHours(actualHours);
		project.setDifficulty(difficulty);
		project.setNotes(notes);

		Project dbProject = projectService.addProject(project);
		System.out.println("Project created successfully: " + dbProject + "\n");

	}

	private BigDecimal getDecimalInput(String prompt) {
		String input = getStringInput(prompt);

		if (Objects.isNull(input)) {
			return null;
		}

		try {
			return new BigDecimal(input).setScale(2);

		} catch (NumberFormatException e) {
			throw new DbException(input + " is not a valid decimal number.");
		}
	}

	private Integer validDifficulty(String prompt) {
		Integer input = getIntInput(prompt);

		// returns null if input is null
		if (Objects.isNull(input)) {
			return null;
		}

		// sets difficulty to null unless input is valid (1, 2, 3, 4, or 5)
		if (input < 1 || input > 5) {
			System.out.println("\t\tInvalid input. Difficulty set to null.");
			return null;
		} else {
			return input;
		}
	}

	private boolean exitMenu() {
		System.out.println("Exiting Application.");
		return true;
	}

	private int getUserSelection() {
		printOperations();

		Integer input = getIntInput("Enter a menu selection");

		return Objects.isNull(input) ? -1 : input;
	}

	private void printOperations() {
		System.out.println("Available selections: (Press Enter key to quit)");

		operations.forEach(line -> System.out.println("\t" + line));

		if (Objects.isNull(curProject)) {
			System.out.println("No project currently selected.");
		} else {
			System.out.println("Selected Project: " + curProject + "\n");
		}
	}

	private Integer getIntInput(String prompt) {
		String input = getStringInput(prompt);

		if (Objects.isNull(input)) {
			return null;
		}

		try {
			return Integer.valueOf(input);

		} catch (NumberFormatException e) {
			throw new DbException(input + " is not a valid number. Try again.");
		}
	}

	private String getStringInput(String prompt) {
		System.out.print(prompt + ": ");
		String input = scanner.nextLine();

		return input.isBlank() ? null : input.trim();
	}
}
