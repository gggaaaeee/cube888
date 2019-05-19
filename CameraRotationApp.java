import java.io.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.animation.*;
import javafx.scene.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.transform.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.Cursor;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.transform.Rotate;
import javafx.scene.control.ToolBar;
import javafx.geometry.Orientation;
import javafx.scene.shape.DrawMode;
//import javafx.scene.transform.Rotate;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.event.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.control.Slider;
//import java.util.concurrent.TimeUnit;
//import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
//import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
//import javafx.scene.control.ToggleGroup;

public class CameraRotationApp extends Application 
{
	int cube_side = 8;
	int nb_cube = cube_side*cube_side*cube_side;
	int scene_size = 180;
	float cube_size = 1.0f;
	int stage_width = 1200;
	int iFrame = 0;
	int MAX_CUBE_FRAME = 64;
	PerspectiveCamera camera; //in 3d scene
	Rotate yRotate; //in 3d scene
	SubScene subscene3d;	
	SubScene subscene2d[];		
	Box box3d[][][]= new Box[cube_side][cube_side][cube_side]; //3d boxes in 3d scene
	Box box2d[][][]= new Box[cube_side][cube_side][cube_side]; //x,y,plan

	private class CubeFrame implements Cloneable,java.io.Serializable
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 0xc0be888L;
		boolean led[][][]; //x,y,plan	
		int m_cube_side;
		CubeFrame()
		{
			m_cube_side=8;
			led = new boolean[m_cube_side][m_cube_side][m_cube_side]; //x,y,plan
		};
		
		public CubeFrame clone() throws CloneNotSupportedException 
		{
			return (CubeFrame)super.clone();  
		}
		
		void SetState(int x,int y,int iPlan, boolean b)
		{
			led[x][y][iPlan] = b;
			return;
		}
		
		void CopyToBox(Box[][][] box)
		{
			int x,y,i;
			for (i=0;i<m_cube_side;i++)
			{
				for (y=0;y<m_cube_side;y++)
				{	
					for (x=0;x<m_cube_side;x++)
					{
						SetCubeColor(i,x,y,led[x][y][i]?1:0);
					}
				}
			}
			return;
		}
	    private  void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException 
	    {
		   // l'ordre de lecture doit être le même que l'ordre d'écriture d'un objet
	    	int i=0,x=0,y=0;
	    	m_cube_side = ois.readInt();
	    	led = new boolean[m_cube_side][m_cube_side][m_cube_side];
			for (i=0;i<m_cube_side;i++)
			{
				for (y=0;y<m_cube_side;y++)
				{	
					for (x=0;x<m_cube_side;x++)
					{
						this.led[x][y][i] = (boolean)ois.readBoolean() ;
					}
				}
			}
		   return;
	    }

		// méthode writeObject, utilisée lors de la sérialization
		private  void writeObject(ObjectOutputStream oos) throws IOException 
		{		
		   // écriture de toute ou partie des champs d'un objet
	    	int i=0,x=0,y=0;
	    	oos.writeInt(cube_side);
			for (i=0;i<cube_side;i++)
			{
				for (y=0;y<cube_side;y++)
				{	
					for (x=0;x<cube_side;x++)
					{
						oos.writeBoolean(led[x][y][i]) ;
					}
				}
			}
			return;
		}		
	};
	
	CubeFrame cubeFrame[] = new CubeFrame[MAX_CUBE_FRAME];
	
    private SubScene createSubscene3D() throws Exception 
	{
		int x=0,y=0,i=0;

        Translate pivot = new Translate();
		pivot.setX(0); 
		pivot.setY(0); 
		pivot.setZ(0);  
			
        yRotate = new Rotate(0, Rotate.Y_AXIS);
		Translate[][][] translate = new Translate[cube_side][cube_side][cube_side];       
	  
		for (i=0;i<cube_side;i++)
		{
			for (y=0;y<cube_side;y++)
			{
				for (x=0;x<cube_side;x++)
				{
					//System.out.println("box[%d]="); 
					//Setting the properties of the Box 
					box3d[x][y][i] = new Box();
					box3d[x][y][i].setWidth(cube_size); 
					box3d[x][y][i].setHeight(cube_size);   
					box3d[x][y][i].setDepth(cube_size);       
					box3d[x][y][i].setMaterial(new PhongMaterial(Color.BLACK));

					translate[x][y][i] = new Translate();
					translate[x][y][i].setX(-x*3 + 10); 
					translate[x][y][i].setY(-y*3 + 10); 
					translate[x][y][i].setZ(-i*3 + 10);  

					box3d[x][y][i].getTransforms().addAll(translate[x][y][i]); 
					box3d[x][y][i].setDrawMode(DrawMode.LINE);
				}
			}
		}       
	
        // Create and position camera
        camera = new PerspectiveCamera(true);
        camera.getTransforms().addAll 
		(
                pivot,
                yRotate,
                new Rotate( -10, Rotate.X_AXIS),
                new Translate(0, 0, -40)
        );
		camera.setFieldOfView(50);
		camera.setFarClip(200);
		//camera.setNearClip(1);
	
        // Build the Scene Graph
        Group root3D = new Group();       
        root3D.getChildren().add(camera);
		for (i=0;i<cube_side;i++)
		{
			for (y=0;y<cube_side;y++)
			{	
				for (x=0;x<cube_side;x++)
				{
					root3D.getChildren().add(box3d[x][y][i]);
				}
			}
		}
		
		PointLight pointLight = new PointLight(Color.WHITE);
		pointLight.setTranslateX(100);
		pointLight.setTranslateY(100);
		pointLight.setTranslateZ(-300);
		pointLight.setRotate(90);
		AmbientLight ambient = new AmbientLight();
		root3D.getChildren().addAll(pointLight, ambient);

        // Use a SubScene
        SubScene subScene = new SubScene(root3D,scene_size*2,scene_size*2,false,SceneAntialiasing.BALANCED);
        subScene.setFill(Color.rgb(220,255,220));
        subScene.setCamera(camera);
		subScene.setCursor(Cursor.OPEN_HAND);
		
        return subScene;
    }
	
	private int getCubePlan(Object pScene)
	{
		int i=0;
		SubScene p;
		for (i=0; i <= cube_side; i++)
		{
			p = subscene2d[i];
			if (p == pScene)
			{
				return i;
			}
		}
		i = -1;
		return i;
	}

    private SubScene createSubscene2D(int iPlanIndex) throws Exception 
	{
		int x=0,y=0;
		
        // Create and position camera
        PerspectiveCamera camera2d = new PerspectiveCamera(true);
		//camera2.setFieldOfView(50);
		camera2d.setFarClip(200);
        Translate pivot2d = new Translate();
		pivot2d.setX(0); 
		pivot2d.setY(0); 
		pivot2d.setZ(0);  
        Rotate yRotate2d = new Rotate(0, Rotate.Y_AXIS);
        camera2d.getTransforms().addAll 
		(
            pivot2d,
            yRotate2d,
            new Rotate( 0, Rotate.X_AXIS),
            new Translate(0, 0, -50)
        );

        // Build the Scene Graph
        Group root2D = new Group();       
        root2D.getChildren().add(camera2d);
		
		Translate[][] translate = new Translate[cube_side][cube_side];       
		for (y=0;y<cube_side;y++)
		{
			for (x=0;x<cube_side;x++)
			{
				translate[x][y] = new Translate();
				translate[x][y].setX(-x*3 + 10); 
				translate[x][y].setY(-y*3 + 10); 
				translate[x][y].setZ(0);  
			}
		}
		
		for (y=0;y<cube_side;y++)
		{
			for (x=0;x<cube_side;x++)
			{
				box2d[x][y][iPlanIndex]= new Box();
				box2d[x][y][iPlanIndex].setWidth(   box3d[x][y][iPlanIndex].getWidth()*2); 
				box2d[x][y][iPlanIndex].setHeight(  box3d[x][y][iPlanIndex].getHeight()*2);   
				box2d[x][y][iPlanIndex].setDepth(   box3d[x][y][iPlanIndex].getDepth()*2);       
				box2d[x][y][iPlanIndex].setMaterial(new PhongMaterial(Color.BLACK));
				box2d[x][y][iPlanIndex].getTransforms().addAll(translate[x][y]);
			}
		}       

		for (y=0;y<cube_side;y++)
		{
			for (x=0;x<cube_side;x++)
			{
				root2D.getChildren().add(box2d[x][y][iPlanIndex]);
			}
		}       

		PointLight pointLight = new PointLight(Color.WHITE);
		pointLight.setTranslateX(100);
		pointLight.setTranslateY(100);
		pointLight.setTranslateZ(-300);
		pointLight.setRotate(90);
		AmbientLight ambient = new AmbientLight();
		root2D.getChildren().addAll(pointLight, ambient);

        // Use a SubScene
        SubScene subScene = new SubScene(root2D,scene_size,scene_size,false,SceneAntialiasing.BALANCED);
        subScene.setFill(((iPlanIndex & 1)^((iPlanIndex>>2) & 1)) !=0 ? Color.rgb(202,202,202) : Color.rgb(232,232,232));
        subScene.setCamera(camera2d);
		subScene.setCursor(Cursor.CROSSHAIR);
		
		subScene.setOnMousePressed(new EventHandler<MouseEvent>() 
		{
			
			@Override
			public void handle(MouseEvent event) 
			{
				int x = (int)Math.round((event.getX()-15)/20);
				if (x < 0) x=0;
				if (x > (cube_side-1)) x = cube_side-1;
				x = cube_side-1-x;
				
				int y = (int)Math.round((event.getY()-15)/20);
				if (y < 0) y=0;
				if (y > (cube_side-1)) y = cube_side-1;
				y = cube_side-1-y;
				
				int iPlan = getCubePlan(event.getSource());				
				
				//System.out.format("mouse click detected! %g %g ",event.getX(),event.getY());
				//System.out.format("block[%d,%d] ",x,y);
				//System.out.format("plan[%d] ",iPlan);
				//System.out.format("Button "+event.getButton());
				//System.out.format(" "+event.getSource() + "\n ");				
				int c = event.getButton() == MouseButton.PRIMARY ? 1 : 0;
				SetCubeColor(iPlan,x,y,c);
			}
		});
		
		return subScene;
	}		
		
	private void SetCubeColor(int iPlan,int x,int y, int c)
	{
		if (c != 0)
		{
			//3d
			box3d[x][y][iPlan].setMaterial(new PhongMaterial(Color.rgb(64,64,255)));
			box3d[x][y][iPlan].setDrawMode(DrawMode.FILL);
			// 2d
			box2d[x][y][iPlan].setMaterial(new PhongMaterial(Color.rgb(64,64,255)));
			cubeFrame[iFrame].SetState(x,y,iPlan,true);
		}
		else
		{
			box3d[x][y][iPlan].setMaterial(new PhongMaterial(Color.rgb(0,0,0)));
			box3d[x][y][iPlan].setDrawMode(DrawMode.LINE);
			// 2d
			box2d[x][y][iPlan].setMaterial(new PhongMaterial(Color.rgb(0,0,0)));
			cubeFrame[iFrame].SetState(x,y,iPlan,false);
		}
		if ((x==0)&&(y==0)&&(iPlan==0))
		{
			//System.out.printf("Frame %d len[0][0][0]=%b %n\n",iPlan,box2d[0][0][0]);
		}
	}
	
	private void ButtonReset()
	{
        yRotate.setAngle(0);
	}

	private void ButtonTilted(int angle)
	{
        yRotate.setAngle(angle);	
	}
	
	private void checkBoxLine(boolean bLine)
	{
		int i=0,x=0,y=0;
		for (i=0;i<cube_side;i++)
		{
			for (y=0;y<cube_side;y++)
			{
				for (x=0;x<cube_side;x++)
				{
					box3d[x][y][i].setDrawMode(bLine?DrawMode.LINE:DrawMode.FILL);
				}
			}
		}		
	}
	
    private BorderPane createBorder() throws Exception 
	{
		// 2D
		BorderPane pane = new BorderPane();
		//pane.setLeft(parent);
		
		Button button1 = new Button("Reset Position");
		button1.setOnAction(e->{ButtonReset();});

		Button button2 = new Button("-90 deg");
		button2.setOnAction(e->{ButtonTilted(-90);});
		
		Button button3 = new Button("-45 deg");
		button3.setOnAction(e->{ButtonTilted(-45);});

		Button button4 = new Button("+45 deg");
		button4.setOnAction(e->{ButtonTilted(45);});

		Button button5 = new Button("+90 deg");
		button5.setOnAction(e->{ButtonTilted(90);});
		
		CheckBox checkBox = new CheckBox("Line");
		checkBox.setOnAction(e->{checkBoxLine(checkBox.isSelected());});
		
		ToolBar toolBar = new ToolBar(button1,button2,button3,button4,button5);
		toolBar.setOrientation(Orientation.VERTICAL);
		
		pane.setRight(toolBar);
		pane.setPrefSize(scene_size,scene_size);
		return pane;
	}
	
	private void AddFrame(Slider slider) 
	{
		if (slider.getValue() < slider.getMax())
		{
			iFrame++;
			slider.setValue(iFrame);
			cubeFrame[iFrame].CopyToBox(box3d);
		}
		return;
	}

	private void CopyFrame(Slider slider)
	{
		if (slider.getValue() < slider.getMax())
		{
			slider.setValue(++iFrame);
			try
			{
				CubeFrame pTmp  = cubeFrame[iFrame-1];
				cubeFrame[iFrame].led = pTmp.led;
			}
			catch(Exception e)
			{
				System.out.println("Warning: Some Other exception");
			}
			
			cubeFrame[iFrame].CopyToBox(box3d);
		}
		return;
	}

	private void DelFrame(Slider slider)
	{
		if (slider.getValue() > slider.getMin())
		{
			iFrame--;
			slider.setValue(iFrame);
			cubeFrame[iFrame].CopyToBox(box3d);
		}
		return;
	}

	private void PlayFrame(Slider slider)
	{
        // animate the cube frame.
        Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.seconds(0), 
                        new KeyValue(slider.valueProperty(), 0)
                ),
                new KeyFrame(
                        Duration.seconds(5), 
                        new KeyValue(slider.valueProperty(), MAX_CUBE_FRAME-1)
                )
        );
        timeline.setCycleCount(1);
        timeline.play();
		
		return;
	}
	
    private BorderPane createBorderBottom() throws Exception 
	{
		// 2D
		BorderPane pane = new BorderPane();
		Button button1 = new Button("Next frame");
		Button button2 = new Button("Previous frame");
		Button button3 = new Button("Play");
		Button button4 = new Button("Copy frame");
		Slider slider = new Slider(0, MAX_CUBE_FRAME-1, 0);
	    HBox rooth1 = new HBox();
	    VBox rootv1 = new VBox();

		button1.setOnAction(e->{AddFrame(slider);});
		button2.setOnAction(e->{DelFrame(slider);});
		button4.setOnAction(e->{CopyFrame(slider);});
		button3.setOnAction(e->{PlayFrame(slider);});

		rooth1.getChildren().addAll(button1,button2,button4,button3);	
		rooth1.setSpacing(15);
		
		slider.setShowTickMarks(true);
		slider.setShowTickLabels(true);
		slider.setMinorTickCount(7);
		slider.setMajorTickUnit(8);
		slider.setBlockIncrement(1);
		slider.setSnapToTicks(true);
		slider.setPrefWidth(stage_width*0.9);
		
	    slider.valueProperty().addListener((observable, oldvalue, newvalue) ->
	            {
	                int i = newvalue.intValue();	                
	                iFrame = i;
	                cubeFrame[iFrame].CopyToBox(box3d);
	            } );

		//slider.setOnAction(e->{SliderMove(slider);});

		rootv1.getChildren().addAll(rooth1,slider);	
		rootv1.setSpacing(10);
		
		ToolBar toolBar = new ToolBar(rootv1);
		toolBar.setOrientation(Orientation.HORIZONTAL);
		
		pane.setBottom(toolBar);
		pane.setPrefSize(50,50);
		return pane;
	}

    private void FileSaveMenu()
    {
	    try 
	    {
	    	int i=0;
	        FileOutputStream fileOut = new FileOutputStream("cube888.ser");
	        ObjectOutputStream out   = new ObjectOutputStream(fileOut);
	        for (i=0;i<MAX_CUBE_FRAME;i++)
	        	out.writeObject(cubeFrame[i]);
	        out.close();
	        fileOut.close();
	        System.out.printf("Serialized data is saved in cube888.ser");
	     } 
	    catch (IOException i) 
	    {
	    	i.printStackTrace();
	    }
    }
    
    private void FileLoadMenu()
    {
	    try 
	    {
	    	int i=0;
	        FileInputStream fileIn = new FileInputStream("cube888.ser");
	        ObjectInputStream in = new ObjectInputStream(fileIn);
	        for (i=0;i<MAX_CUBE_FRAME;i++)
	        {
	        	CubeFrame pTmp =(CubeFrame) in.readObject(); 
	        	cubeFrame[i].led = pTmp.led;
	        }
	        in.close();
	        fileIn.close();
	    } 
	    catch (IOException i) 
	    {
	        i.printStackTrace();
	        return;
	    } 
	    catch (ClassNotFoundException c) 
	    {
	        System.out.println("Employee class not found");
	        c.printStackTrace();
	        return;
	    }
	    iFrame=0;
	    cubeFrame[iFrame].CopyToBox(box3d);
	    //slider.setValue(iFrame);
    }
    
    private void FileNewMenu()
    {
    	int i;
    	CubeFrame pTmp; 
        for (i=0;i<MAX_CUBE_FRAME;i++)
        {
        	pTmp = new CubeFrame();
        	cubeFrame[i].led = pTmp.led;
        }
        iFrame = 0;
	    cubeFrame[iFrame].CopyToBox(box3d);        
    }

    private MenuBar CreateMenu()
    {
	    MenuBar menuBar = new MenuBar();
    	
	    // File menu - new, save, exit
	    Menu fileMenu = new Menu("File");
	    MenuItem newMenuItem = new MenuItem("New");
	    MenuItem saveMenuItem = new MenuItem("Save");
	    MenuItem loadMenuItem = new MenuItem("Load");
	    MenuItem exitMenuItem = new MenuItem("Exit");
	    
	    exitMenuItem.setOnAction(actionEvent -> Platform.exit());
	    saveMenuItem.setOnAction(actionEvent -> FileSaveMenu());
	    loadMenuItem.setOnAction(actionEvent -> FileLoadMenu());
	    newMenuItem.setOnAction(actionEvent -> FileNewMenu());
	    
	    fileMenu.getItems().addAll(newMenuItem, saveMenuItem, loadMenuItem ,new SeparatorMenuItem(),exitMenuItem);

	    menuBar.getMenus().addAll(fileMenu);
	    return menuBar;
    }
	
    private Parent createDesk() throws Exception 
	{
		int i=0;
		subscene3d = createSubscene3D();	
		subscene2d = new SubScene[cube_side];		
		for (i=0;i<cube_side;i++)
		{
			subscene2d[i]  = createSubscene2D(i);
		}
	
	    HBox rooth  = new HBox();
	    HBox root3d = new HBox();
	    HBox rooth1 = new HBox();
	    HBox rooth2 = new HBox();
	    VBox rootv1 = new VBox();
	    VBox rootv2 = new VBox();
		
		root3d.getChildren().addAll(subscene3d);
		root3d.setSpacing(5);
		for (i=0;i<cube_side;i++)
		{
			if (i < (cube_side/2))
				rooth1.getChildren().add(subscene2d[cube_side-1-i]);
			else
				rooth2.getChildren().add(subscene2d[cube_side-1-i]);
		}
		rooth1.setSpacing(5);
		rooth2.setSpacing(5);
		rootv1.getChildren().addAll(rooth1,rooth2);	
		rootv1.setSpacing(5);
		rooth.getChildren().addAll(root3d,rootv1);
		rooth.setSpacing(5);
		
		BorderPane paneBottom = createBorderBottom();
		
		MenuBar menuBar = CreateMenu();
		rootv2.getChildren().addAll(menuBar,rooth,paneBottom);	
					
		return rootv2;
	}

    @Override
    public void start(Stage stage) throws Exception 
	{
		int i=0;
        stage.setResizable(true);
		stage.setWidth(stage_width);
		stage.setHeight(500);
			 		
		//BorderPane paneBottom = createBorderBottom();
		BorderPane pane = createBorder();
		Parent desk = createDesk(); 		
		
		pane.setLeft(desk);
		//pane.setBottom(paneBottom);

		Scene scene =  new Scene(pane);
					
        stage.setScene(scene);
		
		for (i=0;i<MAX_CUBE_FRAME; i++)
		{
			cubeFrame[i] = new CubeFrame();
		}
		cubeFrame[iFrame].CopyToBox(box3d);
		
        stage.show();
    }

    public static void main(String[] args) 
	{
        launch(args);
    }
}