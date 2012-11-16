package mygame.model.character;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;

import com.jme3.asset.plugins.ZipLocator;

import com.jme3.asset.AssetManager;

import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;

import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import java.util.ArrayList;
import java.util.List;
import mygame.States.Scenario.Scenario;
import mygame.model.weapon.Gun;
import mygame.model.weapon.WeaponInterface;

/**
 * Example 9 - How to make walls and floors solid.
 * This collision code uses Physics and a custom Action Listener.
 * @author normen, with edits by Zathras
 */
public class CharacterMainJMonkey extends AbstractAppState
	implements ActionListener, Character {
		
        private BulletAppState bulletAppState;
        //private RigidBodyControl landscape;
        private WeaponInterface currentWeapon;
        private List<WeaponInterface> weapons;
        private CharacterControl playerControl;
        private Node playerModelPorra;
        private AssetManager assetManager;
        private Vector3f walkDirection = new Vector3f();
        private boolean left = false, right = false, up = false, down = false, run = false, turnLeft = false, turnRight = false;
        private SimpleApplication app;
        private AudioNode audio_footstep;
        boolean musica = false;
        boolean primeraVez = true;
        private int contadorMute = 1;
        private int contadorPause = 2;
        boolean isPaused;
        AudioNode audio_environment;
        private Geometry geom1;  
        Node shootables;
        Geometry marcaVermella;
        Scenario escenari;
        protected BitmapFont guiFont;
        private Node pivot;

        @Override
        public void initialize(AppStateManager stateManager, Application applicooter){
                super.initialize(stateManager, applicooter);
                this.app = (SimpleApplication)applicooter;
                this.assetManager  = app.getAssetManager();  
                this.escenari = new Scenario(app);
                this.weapons = new ArrayList<WeaponInterface>();

                setUpKeys();
                inicialitzarMarca();
                initMirilla();
                CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(3f, 4f, 1);
                playerControl = new CharacterControl(capsuleShape, 0.05f);
                playerControl.setJumpSpeed(40);
                playerControl.setFallSpeed(100);
                playerControl.setGravity(100);
                
                pivot = new Node();
                playerModelPorra = (Node) app.getAssetManager().loadModel("Character/porra.j3o");
                //Material playerMaterial = app.getAssetManager().loadMaterial("Character/Cube.002.j3m");
                pivot.attachChild(playerModelPorra);
                //playerModelPorra.addControl(playerControl);
                pivot.addControl(playerControl);
                playerModelPorra.move(0f,-4f,0f);
                
                playerControl.setPhysicsLocation(new Vector3f(0, 5, 0));
                bulletAppState.getPhysicsSpace().add(playerControl);
                app.getRootNode().attachChild(pivot);

                shootables = new Node ("Shootables");
                app.getRootNode().attachChild(shootables);
                shootables.attachChild(escenari.getEscenari());


                initAudio();

                //@Emilio, inicia musica de fondo
                initAmbientAudio();
                //MODIFICAR TEAM DEL PERSONAJE
                //modelo pj de prueba para las colisiones
                /*Mesh mesh1 = new Box(0.5f, 0.5f, 0.5f);
                 geom1 = new Geometry("Personaje", mesh1);
                 geom1.addControl(playerControl);

                 bulletAppState.getPhysicsSpace().add(geom1);*/
        }

        //@Emilio inicia musica ambiente.
        private void initAmbientAudio(){
                if(primeraVez){                             //
                        isPaused = false;
                        audio_environment = new AudioNode(assetManager, "Sounds/Environment/Dark_music_Vampirical.ogg",false);
                        audio_environment.setLooping(true);
                        audio_environment.setVolume(0.3f);
                        reproducirAudio();
                        primeraVez = false;
                }else{

                }  

        }

        public void setState(BulletAppState state){
                bulletAppState=state;
        }

        /** We over-write some navigational key mappings here, so we can
         * add physics-controlled walking and jumping: */
        /*
         * @Emilio Añadido mute y pause.
         */
        public void setUpKeys() {
                app.getInputManager().addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
                app.getInputManager().addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
                app.getInputManager().addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
                app.getInputManager().addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
                app.getInputManager().addMapping("Run", new KeyTrigger(KeyInput.KEY_SPACE));

                app.getInputManager().addMapping("Jump", new KeyTrigger(KeyInput.KEY_J));
                app.getInputManager().addMapping("Mute", new KeyTrigger(KeyInput.KEY_M));
                app.getInputManager().addMapping("Paused", new KeyTrigger(KeyInput.KEY_P));

                app.getInputManager().addMapping("Shoot",
                                                                                 new MouseButtonTrigger(MouseInput.BUTTON_LEFT)); 
                app.getInputManager().addListener(accioDisparar, "Shoot");

                app.getInputManager().addListener(this, "Left");
                app.getInputManager().addListener(this, "Right");
                app.getInputManager().addListener(this, "Up");
                app.getInputManager().addListener(this, "Down");
                app.getInputManager().addListener(this, "Run");

                app.getInputManager().addListener(this, "Jump");
                app.getInputManager().addListener(this, "Mute");
                app.getInputManager().addListener(this, "Paused");

        }

        /** These are our custom actions triggered by key presses.
         * We do not walk yet, we just keep track of the direction the user pressed. */
        public void onAction(String binding, boolean value, float tpf) {

                if(!isPaused){                              //@Emilio nuevo, para pausar
                        if (binding.equals("Left")) {
                                left = value;
                        } else if (binding.equals("Right")) {
                                right = value;
                        } else if (binding.equals("Up")) {
                                up = value;
                        } else if (binding.equals("Run")) {
                                run = value;
                        } else if (binding.equals("Down")) {
                                down = value;
                        } else if (binding.equals("Jump")) {
                                playerControl.jump();
                        } 
                }

                if (binding.equals("Mute")) {               //@Emilio nuevo, para mutear
                        if(contadorMute != 0){
                                reproducirAudio();
                                contadorMute--;
                        }else{
                                contadorMute = 1;
                        } 
                } 
                if (binding.equals("Paused")){               //@Emilio nuevo, para pausar
                        if(!isPaused){
                                if(contadorPause==2){
                                        isPaused = true; 
                                }
                        }else{
                                if(contadorPause==0){
                                        isPaused = false;
                                        contadorPause = 4;
                                }
                        }  
                        contadorPause--;
                }
                //else if (binding.equals("Jump")) {
                //player.jump();
                //}
                if (left || right || up || down) audio_footstep.play();
                else audio_footstep.pause();
        }
		
    /**
     * This is the main event loop--walking happens here.
     * We check in which direction the playerControl is walking by interpreting
     * the camera direction forward (camDir) and to the side (camLeft).
     * The setWalkDirection() command is what lets a physics-controlled playerControl walk.
     * We also make sure here that the camera moves with playerControl.
     */


    public void personatgeUpdate() {
            Vector3f camDir = app.getCamera().getDirection().clone().multLocal(0.6f);
            Vector3f camLeft = app.getCamera().getLeft().clone().multLocal(0.4f);
            Vector3f viewDirection = new Vector3f();
            walkDirection.set(0, 0, 0);

            if (left)  { walkDirection.addLocal(camLeft); }
            if (right) { walkDirection.addLocal(camLeft.negate()); }
            if (up)    { walkDirection.addLocal(camDir); }
            if (down)  { walkDirection.addLocal(camDir.negate()); }
            if (run)  { walkDirection.mult(3); }

            viewDirection.set(new Vector3f(camDir.getX(), 0, camDir.getZ()));

            playerControl.setWalkDirection(walkDirection);
            playerControl.setViewDirection(viewDirection.negate());

            app.getCamera().setLocation(playerControl.getPhysicsLocation());
    }
		
    public Vector3f getPlayerPosition(){
            return playerControl.getPhysicsLocation();
    }
    
    public void addWeapon(WeaponInterface weapon) {
        weapons.add(weapon);
    }
    
    public WeaponInterface getCurrentWeapon() {
        return currentWeapon;
    }


    private void initAudio(){
            audio_footstep = new AudioNode(app.getAssetManager(), "Sounds/Effects/footsteps.wav",false);
            audio_footstep.setLooping(true);
            audio_footstep.setVolume(5.0f);
            app.getRootNode().attachChild(audio_footstep);
    }

    //@Emilio nuevo, reproducir audio.
    private void reproducirAudio(){
            if(musica){
                    audio_environment.pause();
                    musica = false;
            }else{
                    audio_environment.play();
                    musica = true;
            }  
    }
		
    private ActionListener accioDisparar = new ActionListener() {
        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("Shoot") && !keyPressed) {
                CollisionResults resultat = new CollisionResults();
                Ray raig = new Ray(app.getCamera().getLocation(), app.getCamera().getDirection());
                shootables.collideWith(raig, resultat);
                System.out.println("----- Collisions? " + resultat.size() + "-----");
                for (int i = 0; i < resultat.size(); i++) {
                    // For each hit, we know distance, impact point, name of geometry.
                    float dist = resultat.getCollision(i).getDistance();
                    Vector3f pt = resultat.getCollision(i).getContactPoint();
                    String hit = resultat.getCollision(i).getGeometry().getName();
                    System.out.println("* Collision #" + i);
                    System.out.println("  You shot " + hit + " at " + pt + ", " + dist + " wu away.");
                }
                if (resultat.size() > 0) {
                    // The closest collision point is what was truly hit:
                    CollisionResult closest = resultat.getClosestCollision();
                    // Let's interact - we mark the hit with a red dot.
                    marcaVermella.setLocalTranslation(closest.getContactPoint());
                    app.getRootNode().attachChild(marcaVermella);
                } else {
                    // No hits? Then remove the red mark.
                    app.getRootNode().detachChild(marcaVermella);
                }              
            }          
        }
    };

    protected void inicialitzarMarca() {
        Sphere sphere = new Sphere(30, 30, 0.2f);
        marcaVermella = new Geometry("BOOM!", sphere);
        Material marcaVermella_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        marcaVermella_mat.setColor("Color", ColorRGBA.Red);
        marcaVermella.setMaterial(marcaVermella_mat);
    }

    protected void initMirilla() {
        app.getGuiNode().detachAllChildren();
        guiFont = loadGuiFont();
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+"); // crosshairs
        AppSettings settings = new AppSettings(true);
        ch.setLocalTranslation( // center
        settings.getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2,
        settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
        app.getGuiNode().attachChild(ch);
    }

    protected BitmapFont loadGuiFont() {
        return assetManager.loadFont("Interface/Fonts/Default.fnt");
    }

    public int getNLives() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getEnergy() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    public void setCurrentWeapon(WeaponInterface weapon) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void incrementEnergy(double quantity) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void incrementNLives(int quantity) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
