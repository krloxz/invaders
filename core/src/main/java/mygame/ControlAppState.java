package mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;
import com.simsilica.es.Filters;

import java.util.Iterator;

import de.lessvoid.nifty.Nifty;

public class ControlAppState extends AbstractAppState {

    private static final String MOVE_RIGHT = "MOVE_RIGHT";
    private static final String MOVE_LEFT = "MOVE_LEFT";
    private static final String SHOOT = "SHOOT";
    private SimpleApplication app;
    private EntityData ed;
    private Vector3f position;
    private EntitySet ship;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {

        super.initialize(stateManager, app);
        this.app = (SimpleApplication) app;

        ed = this.app.getStateManager().getState(EntityDataState.class).getEntityData();
        ship = ed.getEntities(
                Filters.fieldEquals(Model.class, "name", Model.SpaceShip),
                Model.class,
                Position.class);

        this.position = new Vector3f(0, -20, 0);
        this.app.getInputManager().addMapping(MOVE_LEFT, new MouseAxisTrigger(MouseInput.AXIS_X, true));
        this.app.getInputManager().addMapping(MOVE_RIGHT, new MouseAxisTrigger(MouseInput.AXIS_X, false));

        this.app.getInputManager().addListener(analogListener, MOVE_LEFT, MOVE_RIGHT);

        this.app.getInputManager().addMapping(SHOOT,
                new KeyTrigger(KeyInput.KEY_SPACE),
                new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        this.app.getInputManager().addListener(actionListener, SHOOT);
    }
    private final AnalogListener analogListener = new AnalogListener() {
        @Override
        public void onAnalog(String name, float value, float tpf) {
            if (name.equals(MOVE_LEFT) || name.equals(MOVE_RIGHT)) {
                Vector2f mousePos = app.getInputManager().getCursorPosition();
                float x = FastMath.clamp((mousePos.getX() - app.getCamera().getWidth() / 2) * 0.05f, -22, 22);
                position = new Vector3f(x, -20, 0);
            }
        }
    };
    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals(SHOOT) && !isPressed) {
                ship.applyChanges();
                Vector3f shipLocation = getShip().get(Position.class).getLocation();
                EntityId bullet = ed.createEntity();
                ed.setComponents(bullet,
                        new Model(Model.Bullet),
                        new Attack(1),
                        new CollisionShape(0.25f),
                        new Position(new Vector3f(shipLocation.getX(), shipLocation.getY() + 3.5f, 0)),
                        new Speed(20),
                        new Decay(2000));
                showMessage();
            }
        }
    };

    @Override
    public void update(float tpf) {
        ship.applyChanges();
        getShip().set(new Position(position));
    }

    @Override
    public void cleanup() {
    }

    private Entity getShip() {
        final Iterator<Entity> iterator = ship.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        throw new NullPointerException("Ship not found");
    }

    private void showMessageBack() {
        final BitmapFont font = this.app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        final BitmapText text = new BitmapText(font);
        text.setSize(font.getCharSet().getRenderedSize());
        text.move(new Vector3f(100, 100, 0));
        this.app.getGuiNode().attachChild(text);
        text.setText("Test!");
    }

    private void showMessage() {
        final NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(
                this.app.getAssetManager(),
                this.app.getInputManager(),
                this.app.getAudioRenderer(),
                this.app.getGuiViewPort());
        final Nifty nifty = niftyDisplay.getNifty();
        nifty.fromXml("Interface/GameOver.xml", "start");
        this.app.getGuiViewPort().addProcessor(niftyDisplay);
    }
}