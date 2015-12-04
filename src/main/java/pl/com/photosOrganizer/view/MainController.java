package pl.com.photosOrganizer.view;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import pl.com.photosOrganizer.MainApp;

public class MainController {
	@SuppressWarnings("unused")
	private MainApp mainApp;
	private Stage stage;
	private File selectedDirectory;
	
	private int filesNumber, fileCounter;

	@FXML
	private ProgressBar progressBar;
	
	@FXML
	private TextField directoryPath;
	
	@FXML 
	private Button runButton; 

	Task<Void> task = new Task<Void>() {
		@Override
		protected Void call() throws Exception {
			fileCounter = 0;
			Path path = Paths.get(selectedDirectory.getPath());
			DateFormat df = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");

			try (Stream<Path> stream = Files.list(path)) {
				stream.map(Path::toFile).filter(file -> !file.isDirectory()).sorted().collect(Collectors.toList())
						.stream().map(File::toPath).forEach((p -> {
					try {
						Date date = new Date(
								Files.readAttributes(p, BasicFileAttributes.class).creationTime().toMillis());
						String extendsion = p.toFile().getName().substring(p.toFile().getName().lastIndexOf("."));
						File checker = new File(path.toAbsolutePath() + "/" + df.format(date) + extendsion);
						int counter = 0;
						while (checker.exists()) {
							counter++;
							checker = new File(
									path.toAbsolutePath() + "/" + df.format(date) + "_" + counter + extendsion);
						}
						if (counter == 0)
							Files.move(p, p.resolveSibling(df.format(date) + extendsion));
						else
							Files.move(p, p.resolveSibling(df.format(date) + "_" + counter + extendsion));
						fileCounter++;
						progressBar.setProgress(fileCounter/filesNumber);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}));
			}
			return null;
		}
	};

	/**
	 * Initializes the controller class. This method is automatically called
	 * after the fxml file has been loaded.
	 */
	@FXML
	private void initialize() {
		// Initialize the person table with the two columns.
	}

	@FXML
	private void handleSelectFolder() {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Choose folder with images");
		selectedDirectory = chooser.showDialog(this.stage);
		if(selectedDirectory!=null){
			directoryPath.setText(selectedDirectory.getAbsolutePath());
			filesNumber = selectedDirectory.list((dir,name)->dir.isDirectory()).length;
			runButton.setDisable(false);
		}else{
			runButton.setDisable(true);
		}
	}

	@FXML
	private void handleStratProcess() {
		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();
		
		System.out.println(selectedDirectory.getAbsolutePath());
	}

	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}

	public void setStage(Stage primaryStage) {
		this.stage = primaryStage;

	}
}
