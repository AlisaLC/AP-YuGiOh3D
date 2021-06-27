package edu.sharif.ce.apyugioh.view.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.sharif.ce.apyugioh.YuGiOh;
import edu.sharif.ce.apyugioh.controller.AssetController;
import edu.sharif.ce.apyugioh.controller.DeckMenuController;
import edu.sharif.ce.apyugioh.model.DatabaseManager;
import edu.sharif.ce.apyugioh.model.Deck;
import edu.sharif.ce.apyugioh.model.Inventory;
import edu.sharif.ce.apyugioh.model.card.Card;
import edu.sharif.ce.apyugioh.view.ButtonClickListener;
import edu.sharif.ce.apyugioh.view.model.CardActor;
import lombok.Getter;
import lombok.Setter;

public class DeckMenuView extends Menu {

    private Stage stage;
    private Texture backgroundTexture;

    private Window inventoryWindow;
    private CardsContainer inventoryCards;

    private Window cardPreviewWindow;

    private Window sideDeckWindow;
    private CardsContainer sideDeckCards;

    private Window mainDeckWindow;
    private CardsContainer mainDeckCards;

    private Window decksListWindow;
    private Table decksListTable;
    private TextButton backButton;
    private TextButton activateButton;
    private TextButton deleteDeckButton;
    private TextButton newDeckButton;

    private Window activeDeckWindow;
    private DeckListElement activeDeckPreview;

    private SpriteBatch batch;
    private CardActor selectedCard;
    private CardActor draggingCard;
    private CardsContainer draggingCardContainer;

    public DeckMenuView(YuGiOh game) {
        super(game);
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new PointLight().set(0.8f, 0.8f, 0.8f, 15, 5, 0, 150));
        environment.add(new DirectionalLight().set(0.35f, 0.35f, 0.35f, 0.1f, -0.03f, -0.1f));
        assets.load("3D/puzzle/puzzle.g3db", Model.class);
        batch = new SpriteBatch();
        stage = new Stage();
        backgroundTexture = new Texture(Gdx.files.internal("backgrounds/main" + MathUtils.random(1, 10) + ".jpg"));
    }

    @Override
    public void show() {
        super.show();
        initialize();
        updateCardContainers();
        updateDeckList();
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();
        stage.act();
        stage.draw();
        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT) && draggingCard != null)
            dragCard(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
        if (draggingCard != null) {
            draggingCard.setPosition(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
            stage.getBatch().begin();
            draggingCard.draw(stage.getBatch(), 1);
            stage.getBatch().end();
        }
        lastX = Gdx.input.getX();
        lastY = Gdx.input.getY();
    }

    @Override
    public void dispose() {
        super.dispose();
        stage.dispose();
    }

    private void initialize() {
        inventoryCards = new CardsContainer(5, 230, 340, 30);
        sideDeckCards = new CardsContainer(3, 170, 170 * 1.6f, 20);
        mainDeckCards = new CardsContainer(5, 170, 170 * 1.6f, 20);
        selectedCard = new CardActor();
        inventoryWindow = new Window("", AssetController.getSkin("first"));
        cardPreviewWindow = new Window("", AssetController.getSkin("first"));
        sideDeckWindow = new Window("", AssetController.getSkin("first"));
        mainDeckWindow = new Window("", AssetController.getSkin("first"));
        decksListWindow = new Window("", AssetController.getSkin("first"));
        decksListTable = new Table();
        activeDeckWindow = new Window("current Deck", AssetController.getSkin("first"));
        backButton = new TextButton("Back", AssetController.getSkin("first"));
        activateButton = new TextButton("Activate", AssetController.getSkin("first"));
        newDeckButton = new TextButton("New Deck...", AssetController.getSkin("first"));
        deleteDeckButton = new TextButton("delete Deck", AssetController.getSkin("first"));
        stage.addActor(inventoryWindow);
        stage.addActor(cardPreviewWindow);
        stage.addActor(sideDeckWindow);
        stage.addActor(mainDeckWindow);
        stage.addActor(decksListWindow);
        stage.addActor(activeDeckWindow);
        arrangeWidgets();
        addListeners();
    }

    private void arrangeWidgets() {
        arrangeWindows();
        selectedCard.setWidth(cardPreviewWindow.getWidth() * 0.8f);
        selectedCard.setHeight(cardPreviewWindow.getHeight() * 0.8f);
        cardPreviewWindow.add(selectedCard).fill().center();
        ScrollPane inventoryScrollPane = new ScrollPane(inventoryCards.getCardsTable(), AssetController.getSkin("first"));
        inventoryScrollPane.setFillParent(true);
        inventoryScrollPane.setFlickScroll(false);
        inventoryWindow.add(inventoryScrollPane).fill().padRight(10).padLeft(10).center();
        ScrollPane sideDeckScrollPane = new ScrollPane(sideDeckCards.getCardsTable(), AssetController.getSkin("first"), "vertical");
        sideDeckScrollPane.setFillParent(true);
        sideDeckScrollPane.setFlickScroll(false);
        sideDeckScrollPane.setScrollbarsVisible(false);
        sideDeckWindow.add(sideDeckScrollPane).fill().left();
        ScrollPane mainDeckScrollPane = new ScrollPane(mainDeckCards.getCardsTable(), AssetController.getSkin("first"));
        mainDeckScrollPane.setFillParent(true);
        mainDeckScrollPane.setFlickScroll(false);
        mainDeckWindow.add(mainDeckScrollPane).fill().left();
        ScrollPane deckScrollPane = new ScrollPane(decksListTable,AssetController.getSkin("first"),"horizontal");
        deckScrollPane.setSize(decksListWindow.getWidth()*0.9f,decksListWindow.getHeight()*0.8f);
        deckScrollPane.setPosition(decksListWindow.getX() + 20,decksListWindow.getY()+60);
        stage.addActor(deckScrollPane);
        backButton.setSize(decksListWindow.getWidth()*0.2f,60);
        newDeckButton.setSize(decksListWindow.getWidth()*0.2f,60);
        deleteDeckButton.setSize(decksListWindow.getWidth()*0.2f,60);
        activateButton.setSize(decksListWindow.getWidth()*0.2f,60);

        backButton.setPosition(decksListWindow.getX()+110,decksListWindow.getY()+10);
        newDeckButton.setPosition(backButton.getX() +backButton.getWidth() +20,decksListWindow.getY()+10);
        deleteDeckButton.setPosition(newDeckButton.getX() + newDeckButton.getWidth() +20,decksListWindow.getY()+10);
        activateButton.setPosition(deleteDeckButton.getX()+ deleteDeckButton.getWidth() + 20,decksListWindow.getY()+10);
        decksListWindow.setTouchable(Touchable.disabled);
        stage.addActor(backButton);
        stage.addActor(newDeckButton);
        stage.addActor(deleteDeckButton);
        stage.addActor(activateButton);
        //stage.addActor(decksListTable);
        activeDeckPreview = new DeckListElement("----",DeckMenuController.getInstance().getUser().getId(),
                activeDeckWindow.getWidth()*0.5f,activeDeckWindow.getHeight()*0.85f,12);
        activeDeckWindow.add(activeDeckPreview).expand().fill();
    }

    private void arrangeWindows() {
        float horizontalPad = 20;
        float verticalPad = 20;
        float height = Gdx.graphics.getHeight() - verticalPad * 2 - 80;
        decksListWindow.setBounds(60, 40, 1500, height * 2 / 9f);
        activeDeckWindow.setBounds(decksListWindow.getX() + decksListWindow.getWidth() + horizontalPad,
                40, 240, height * 2 / 9f);
        sideDeckWindow.setBounds(60, decksListWindow.getY() + decksListWindow.getHeight() + verticalPad,
                650, height * 3 / 9f);
        mainDeckWindow.setBounds(sideDeckWindow.getX() + sideDeckWindow.getWidth() + horizontalPad,
                decksListWindow.getY() + decksListWindow.getHeight() + verticalPad,
                1100, height * 3 / 9f);
        inventoryWindow.setBounds(60, sideDeckWindow.getY() + sideDeckWindow.getHeight() + verticalPad,
                1440, height * 4 / 9f);
        cardPreviewWindow.setBounds(inventoryWindow.getX() + inventoryWindow.getWidth() + horizontalPad,
                sideDeckWindow.getY() + sideDeckWindow.getHeight() + verticalPad,
                300, height * 4 / 9f);
    }

    private void addListeners() {
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE)
                    DeckMenuController.getInstance().back();
                return super.keyDown(event, keycode);
            }
        });
        backButton.addListener(new ButtonClickListener() {
            @Override
            public void clickAction() {
                DeckMenuController.getInstance().back();
            }
        });
        newDeckButton.addListener(new ButtonClickListener() {
            @Override
            public void clickAction() {
                showDeckNameAskDialog();
            }
        });
        deleteDeckButton.addListener(new ButtonClickListener() {
            @Override
            public void clickAction() {
                deleteDeck();
            }
        });
        activateButton.addListener(new ButtonClickListener() {
            @Override
            public void clickAction() {
                activateDeck();
            }
        });
    }

    public void selectCard(CardActor cardActor, CardsContainer container) {
        draggingCardContainer = container;
        draggingCard = CardActor.clone(cardActor);
        selectedCard.getCardSprite().setTexture(cardActor.getCardSprite().getTexture());
    }

    public void updateCardContainers(){
        loadUserInventory();
        loadSideDeckCards(DeckMenuController.getInstance().getSelectedDeck());
        loadMainDeckCards(DeckMenuController.getInstance().getSelectedDeck());
    }

    public void updateDeckList(){
        DeckMenuController.getInstance().loadUserDecks();
        activeDeckPreview.setIsSelectable(false);
        activeDeckPreview.setDeck(DeckMenuController.getInstance().getUserActiveDeck());
        List<DeckListElement> deckElements = new ArrayList<>();
        for(Deck deck:DeckMenuController.getInstance().getUserDecks()){
            deckElements.add(new DeckListElement(deck.getName(),DeckMenuController.getInstance().getUser().getId(),
                    100,160,9));
        }
        decksListTable.clearChildren();
        for(DeckListElement deckListElement: deckElements){
            decksListTable.add(deckListElement).padRight(20).padLeft(20);
            deckListElement.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    selectDeck(deckListElement);
                }
            });
        }
    }

    public void loadUserInventory() {
        DeckMenuController.getInstance().loadUserInventory();
        Inventory userInventory = DeckMenuController.getInstance().getUserInventory();
        Map<String, Integer> inventoryMonsters = userInventory.getMonsters();
        Map<String, Integer> inventorySpells = userInventory.getSpells();
        Map<String, Integer> inventoryTraps = userInventory.getTraps();
        inventoryCards.updateCards(inventoryMonsters, inventorySpells, inventoryTraps);
    }

    private void newDeck(String deckName) {
        DeckMenuController.getInstance().createDeck(deckName);
        updateDeckList();
    }

    private void deleteDeck() {
        DeckMenuController.getInstance().deleteDeck();
        updateDeckList();
        updateCardContainers();
    }

    private void activateDeck() {
        DeckMenuController.getInstance().activateDeck();
        updateDeckList();
    }

    public void selectDeck(DeckListElement selectedDeck) {
        DeckMenuController.getInstance().selectDeck(selectedDeck.getDeck());
        loadDeck(selectedDeck.getDeck());
        updateDeckList();
    }

    private void loadDeck(Deck deck) {
        loadMainDeckCards(deck);
        loadSideDeckCards(deck);
    }

    private void loadMainDeckCards(Deck deck) {
        if (deck == null) {
            mainDeckCards.updateCards(null, null, null);
            return;
        }
        Map<String, Integer> mainDeckMonsters = deck.getMonsters(false);
        Map<String, Integer> mainDeckSpells = deck.getSpells(false);
        Map<String, Integer> mainDeckTraps = deck.getTraps(false);
        mainDeckCards.updateCards(mainDeckMonsters, mainDeckSpells, mainDeckTraps);
    }

    private void loadSideDeckCards(Deck deck) {
        if (deck == null) {
            sideDeckCards.updateCards(null, null, null);
            return;
        }
        Map<String, Integer> sideDeckMonsters = deck.getMonsters(true);
        Map<String, Integer> sideDeckSpells = deck.getSpells(true);
        Map<String, Integer> sideDeckTraps = deck.getTraps(true);
        sideDeckCards.updateCards(sideDeckMonsters, sideDeckSpells, sideDeckTraps);
    }

    private void dragCard(float mouseX, float mouseY) {
        //dragged to Inventory
        if (isInsideWindow(inventoryWindow, mouseX, mouseY)) {
            if (draggingCardContainer != inventoryCards) {
                if (draggingCardContainer == mainDeckCards)
                    removeCardFromMainDeck(draggingCard.getCard());
                if (draggingCardContainer == sideDeckCards)
                    removeCardFromSideDeck(draggingCard.getCard());
            }
        }
        //dragged to sideDeck
        if (isInsideWindow(sideDeckWindow, mouseX, mouseY))
            addCardToSideDeck(draggingCard.getCard());
        //dragged to mainDeck
        if (isInsideWindow(mainDeckWindow, mouseX, mouseY))
            addCardToMainDeck(draggingCard.getCard());
        draggingCard = null;
        draggingCardContainer = null;
        updateCardContainers();
    }

    private void addCardToMainDeck(Card card) {
        if (draggingCardContainer == mainDeckCards)
            return;
        if (draggingCardContainer == sideDeckCards)
            removeCardFromSideDeck(card);
        DeckMenuController.getInstance().addCardToMainDeck(card);
    }

    private void addCardToSideDeck(Card card) {
        if (draggingCardContainer == sideDeckCards)
            return;
        if (draggingCardContainer == mainDeckCards)
            removeCardFromMainDeck(card);
        DeckMenuController.getInstance().addCardToSideDeck(card);
    }

    private void removeCardFromMainDeck(Card card){
        DeckMenuController.getInstance().removeCardFromMainDeck(card);
    }

    private void removeCardFromSideDeck(Card card){
        DeckMenuController.getInstance().removeCardFromSideDeck(card);
    }

    private void showDeckNameAskDialog(){
        Dialog dialog = new Dialog("Enter New Deck's Name:",AssetController.getSkin("first"));
        TextButton okButton = new TextButton("Ok",AssetController.getSkin("first"));
        TextButton cancelButton = new TextButton("Cancel",AssetController.getSkin("first"));
        TextField deckNameField = new TextField("",AssetController.getSkin("first"));
        deckNameField.setMessageText("New Deck's Name...");
        deckNameField.setAlignment(3);
        dialog.setSize(300,200);
        dialog.setModal(true);
        dialog.setMovable(false);
        dialog.setResizable(false);
        okButton.addListener(new ButtonClickListener() {
            @Override
            public void clickAction() {
                newDeck(deckNameField.getText());
                dialog.hide();
                dialog.cancel();
                dialog.remove();
            }
        });
        cancelButton.addListener(new ButtonClickListener() {
            @Override
            public void clickAction() {
                dialog.hide();
                dialog.cancel();
                dialog.remove();
            }
        });
        dialog.getContentTable().add(deckNameField).fill().expandX().padLeft(50).padRight(50).height(70).colspan(2);
        dialog.getButtonTable().add(cancelButton).fill().expand().colspan(1).height(110);
        dialog.getButtonTable().add(okButton).fill().expand().colspan(1).height(110);
        dialog.show(stage);
    }

    public void showErrorDialog(String errorMessage) {
        Dialog dialog = new Dialog("Enter New Deck's Name:",AssetController.getSkin("first"));
        TextButton okButton = new TextButton("Ok",AssetController.getSkin("first"));
        Label errorMessageLabel = new Label(errorMessage,AssetController.getSkin("first"),"title");
        dialog.setModal(true);
        dialog.setMovable(false);
        dialog.setResizable(false);
        okButton.addListener(new ButtonClickListener() {
            @Override
            public void clickAction() {
                dialog.hide();
                dialog.cancel();
                dialog.remove();
            }
        });
        dialog.getContentTable().add(errorMessageLabel).fill().expandX().padLeft(10).padRight(10);
        dialog.getButtonTable().add(okButton).fill().expand().height(110);
        dialog.show(stage);
    }

    private boolean isInsideWindow(Window window, float x, float y) {
        if (window.getX() < x && window.getX() + window.getWidth() > x
                && window.getY() < y && window.getY() + window.getHeight() > y)
            return true;
        return false;
    }

}

class CardsContainer {
    @Getter
    @Setter
    private Table cardsTable;
    private Map<String, Integer> monsterCards;
    private Map<String, Integer> spellCards;
    private Map<String, Integer> trapCards;
    private int rowCardsCount;
    private float pad = 20;
    private float cardWidth;
    private float cardHeight;

    public CardsContainer(int rowCardsCount, float cardWidth, float cardHeight, float pad) {
        this.rowCardsCount = rowCardsCount;
        this.cardWidth = cardWidth;
        this.cardHeight = cardHeight;
        this.pad = pad;
        cardsTable = new Table();
        monsterCards = new HashMap<>();
        spellCards = new HashMap<>();
        trapCards = new HashMap<>();
    }

    private Card getCardByName(String cardName) {
        return DatabaseManager.getCards().getCardByName(cardName);
    }

    public Table getCardsTable() {
        return cardsTable;
    }

    public void updateCards(Map<String, Integer> monsterCards, Map<String, Integer> spellCards,
                            Map<String, Integer> trapCards) {
        this.monsterCards = monsterCards;
        this.spellCards = spellCards;
        this.trapCards = trapCards;
        loadCards();
    }

    public void setPad(float pad) {
        this.pad = pad;
    }

    public void loadCards() {
        cardsTable.clearChildren();
        ArrayList<CardActor> cardActors = new ArrayList<>();
        if (monsterCards != null) {
            for (String cardName : monsterCards.keySet()) {
                cardActors.add(new CardActor(getCardByName(cardName), 200,
                        340, monsterCards.get(cardName)));
            }
        }
        if (spellCards != null) {
            for (String cardName : spellCards.keySet()) {
                cardActors.add(new CardActor(getCardByName(cardName), 200,
                        340, spellCards.get(cardName)));
            }
        }
        if (trapCards != null) {
            for (String cardName : trapCards.keySet()) {
                cardActors.add(new CardActor(getCardByName(cardName), 200,
                        340, trapCards.get(cardName)));
            }
        }
        for (int i = 0; i < cardActors.size(); ++i) {
            CardActor cardActor = cardActors.get(i);
            makeCardDraggable(cardActor);
            cardActor.setSize(cardWidth, cardHeight);
            if (i % rowCardsCount == 0 && i > 0)
                cardsTable.row();
            cardsTable.add(cardActor).padRight(pad).padTop(pad).center();
        }
    }

    private void makeCardDraggable(CardActor cardActor) {
        CardsContainer thisContainer = this;
        cardActor.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                DeckMenuController.getInstance().getView().selectCard(cardActor, thisContainer);
                return super.touchDown(event, x, y, pointer, button);
            }
        });
    }

}

class DeckListElement extends Actor {

    @Getter
    @Setter
    private Sprite topCardSprite;
    @Getter
    @Setter
    private Deck deck;
    private Label deckNameLabel;
    private int titleLimit;
    private boolean isSelectable;
    private final float nameLabelHeight = 40f;

    public DeckListElement(String deckName,int userId,float width,float height,int titleLimit){
        super();
        isSelectable = true;
        setWidth(width);
        setHeight(height);
        this.titleLimit = titleLimit;
        deckNameLabel = new Label(deckName,AssetController.getSkin("first"),"title");
        topCardSprite = new Sprite();
        topCardSprite.setSize(width,height-nameLabelHeight);
        loadTopMostCard();
        setDeck(Deck.getDeckByName(userId,deckName));
    }

    public void loadTopMostCard(){
        topCardSprite = new Sprite(new Texture(Gdx.files.local("assets/cards/monster/Unknown.jpg")));
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
        if (deck==null)
            return;
        deckNameLabel.setText(deck.getName());
        if (deckNameLabel.getText().length > titleLimit){
            StringBuilder newTitle = new StringBuilder(deckNameLabel.getText());
            newTitle.delete(titleLimit-3,deckNameLabel.getText().length);
            newTitle.append("...");
            deckNameLabel.setText(newTitle.toString());
        }
    }

    public void setIsSelectable(boolean isSelectable) {
        this.isSelectable = isSelectable;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (deck == null)
            return;
        if (isSelectable) {
            if (deck != null && DeckMenuController.getInstance().getSelectedDeck() != null &&
                    DeckMenuController.getInstance().getSelectedDeck().getId() == deck.getId())
                deckNameLabel.getStyle().fontColor = Color.YELLOW;
            else
                deckNameLabel.getStyle().fontColor = Color.WHITE;
        }
        deckNameLabel.setPosition(getX() + (getWidth()-deckNameLabel.getWidth())/2f,getY());
        topCardSprite.setBounds(getX(),getY()+nameLabelHeight,getWidth(),getHeight()-nameLabelHeight);
        super.draw(batch, parentAlpha);
        topCardSprite.draw(batch,parentAlpha);
        deckNameLabel.draw(batch,parentAlpha);
    }
}