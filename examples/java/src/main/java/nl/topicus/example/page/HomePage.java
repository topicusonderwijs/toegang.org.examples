package nl.topicus.example.page;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import nl.topicus.example.client.ToegangOrg;
import nl.topicus.example.exception.JwsConversionException;
import nl.topicus.example.model.JwsPayload;
import nl.topicus.example.security.helper.JwsHelper;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

public class HomePage extends WebPage {

    private static final long serialVersionUID = 1L;

    private Model<String> inputModel = new Model<>("");
    private Model<String> responseTextModel = new Model<>("");
    private Model<String> validationResultModel = new Model<>("");
    private Model<String> validationResultBoxModel = new Model<>("");

    private Label responseInfoText = new Label("responseInfoText", responseTextModel);
    private WebMarkupContainer validationBox = new WebMarkupContainer("validationResultBox", validationResultBoxModel);

    private List<String> JwsDetailValues = new ArrayList<>(Arrays.asList("org", "fn", "sub", "exp", "tlink", "ean", "aud", "ref", "rnd", "rol"));
    private List<Model<String>> JwsDetailModels = new ArrayList<>(Arrays.asList(new Model<>(""), new Model<>(""), new Model<>(""), new Model<>(""), new Model<>(""), new Model<>(""), new Model<>(""), new Model<>(""), new Model<>(""), new Model<>("")));


    public HomePage(final PageParameters parameters) {
        super(parameters);

        add(validationBox.add(new Label("validationResult", validationResultModel)));
        add(responseInfoText);

        ListView<String> lacList = initializeJwsDetails();
        add(lacList);

        // json object wordt gemaakt voor de request
//        try {
//            System.out.println(new ObjectMapper().writeValueAsString(new LicenseRequest("application/json", "Bearer", "This is the token", new LicenseInformation("productId", "distributorId", 200, "requestReferenceId"))));
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }

        Form<String> tokenForm = initializeTokenForm(lacList);

        add(tokenForm);
        tokenForm.add(new SubmitLink("submitToken"));

        TextArea tokenField = new TextArea<>("tokenField", inputModel);
        tokenForm.add(tokenField);

        tokenForm.add(new Link<String>("clearTokenField") {
            @Override
            public void onClick() {
                inputModel.setObject("");
            }
        });
    }

    /**
     * Fabriceert een form met daarin zijn onclick logica
     *
     * @param lacList wordt gebruikt om de lijst aan te passen naar wens
     * @return de form dat wordt aangemaakt
     */
    private Form<String> initializeTokenForm(ListView<String> lacList) {
        return new Form<String>("tokenForm") {

            @Override
            protected void onSubmit() {
                String token = inputModel.getObject();
                try {
                    /*
                     * 1. De token wordt getoetst en de payload wordt terug gegeven, als die valide is, in json formaat
                     * 2. Zet alles wat in de jsonJws variabele zit, in een java object genaamd: 'JwsPayload'
                     * 3. Wijst de validatie doos de juiste kleur toe. Als je true meegeeft dan wordt die groen en rood als false meegeeft
                     * 5. Geeft alle modellen de juiste waardes. Deze worden allemaal uit de JwsPayload instantie gehaald
                     */
                    String jsonJws = JwsHelper.validateJws(token, "Naam van uitgever");
                    JwsHelper.createJwsPayload(jsonJws);
                    tokenIsValid(true);
                    validationResultModel.setObject("Token is valid");
                    setJwsDetails(JwsHelper.getJwsPayload(), JwsDetailModels, lacList);

                } catch (JwsConversionException | JsonParseException | JsonMappingException re) {
                    /*
                     * 3. reset alle waardes die in de modellen zitten
                     */
                    tokenIsValid(false);
                    validationResultModel.setObject(re.getMessage());
                    resetJwsDetails(lacList);

                } catch (IOException io) {
                    tokenIsValid(false);
                    validationResultModel.setObject(io.getMessage());
                    resetJwsDetails(lacList);
                } finally {
                    /*
                     * 1. Stuurt de JwsPayload instantie en de JWS token naar de toegang.org/callback endpoint
                     * 2. De response status krijgt een kleur op basis van de status
                     * 3. Zet de response status in het model, zodat het aan de voorkant getoond wordt
                     */
                    Response response = ToegangOrg.getInstance().sendPayloadAndToken(JwsHelper.getJwsPayload(), token);
                    setResponseColor(response.getStatus());
                    responseTextModel.setObject(""+response.getStatus());
                }
            }
        };
    }

    /**
     * Wijst de kleur toe aan de achtergrond van de validatie doos
     *
     * @param valid als dit true is dan wordt die groen en rood als die false is
     */
    private void tokenIsValid(boolean valid) {
        this.validationBox.add(new AbstractAjaxBehavior() {
            @Override
            public void onRequest() {
            }

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                if (valid) {
                    tag.put("style", "background-color: green; padding: 5px;");
                } else {
                    tag.put("style", "background-color: red; padding: 5px;");
                }
            }
        });
    }

    /**
     * Een kleur wordt op basis van de status code aan de status text toegekend
     *
     * @param responseStatus op basis hiervan wordt de kleur toegekend
     */
    private void setResponseColor(int responseStatus) {
        this.responseInfoText.add(new AbstractAjaxBehavior() {
            @Override
            public void onRequest() {
            }

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                if (responseStatus == 204) {
                    tag.put("style", "color: green; padding: 5px;");
                } else {
                    tag.put("style", "color: red; padding: 5px;");
                }
            }
        });
    }

    /**
     * Zorgt ervoor dat alle velden, die aan de voorkant te zien zijn, worden toegekend aan een model
     *
     * @return een listview dat correct is ingesteld met alle LAC items
     */
    private ListView<String> initializeJwsDetails() {
        ListView<String> lacList = new ListView<String>("lacList", JwsHelper.getJwsPayload().getLac()) {
            @Override
            protected void populateItem(ListItem<String> item) {
                item.add(new Label("lacItem", item.getDefaultModelObjectAsString()));
            }
        };

        for (int i = 0; i < this.JwsDetailValues.size(); i++) {
            add(new Label(this.JwsDetailValues.get(i), this.JwsDetailModels.get(i)));
        }

        return lacList;
    }

    /**
     * Vult alle velden in, die aan de voorkant te zien zijn
     * Zet het vervaldatum van de jws token om naar een leesbare datum
     *
     * @param payload hier haalt die alle informatie uit
     * @param models hier zet die alle informatie in
     * @param lacList hierin komen de items in de LAC lijst
     */
    private void setJwsDetails(JwsPayload payload, List<Model<String>> models, ListView<String> lacList) {
        for (int i = 0; i < payload.getValues().size(); i++) {
            if (i == 3) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(JwsHelper.getExpirationInMs() / 1000);
                models.get(i).setObject("" + c.getTime());
            } else {
                models.get(i).setObject(payload.getValues().get(i));
            }
        }

        lacList.setList(payload.getLac());
    }


    private void resetJwsDetails(ListView<String> lacList) {
        for (int i = 0; i < this.JwsDetailModels.size(); i++) {
            this.JwsDetailModels.get(i).setObject(" ");
        }

        lacList.setList(new ArrayList<>());
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        PackageResourceReference cssFile =
                new PackageResourceReference(this.getClass(), "homepage.css");
        CssHeaderItem cssItem = CssHeaderItem.forReference(cssFile);

        response.render(cssItem);
    }
}
