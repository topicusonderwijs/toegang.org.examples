package nl.topicus.example.page;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import nl.topicus.example.api.LicenseCreator;
import nl.topicus.example.client.ToegangClient;
import nl.topicus.example.exception.JwsConversionException;
import nl.topicus.example.model.JwsPayload;
import nl.topicus.example.model.response.LicenseResponse;
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

    // modellen
    private Model<String> inputModel = new Model<>("");
    private Model<String> licenseLinkModel = new Model<>("");
    private Model<String> startDateModel = new Model<>("");
    private Model<String> endDateModel = new Model<>("");
    private Model<String> responseTextModel = new Model<>("");
    private Model<String> validationResultModel = new Model<>("");
    private Model<String> validationResultBoxModel = new Model<>("");

    // globale validatie elementen
    private Label responseInfoText = new Label("responseInfoText", responseTextModel);
    private WebMarkupContainer validationBox = new WebMarkupContainer("validationResultBox", validationResultBoxModel);

    // token informatie met bijbehorende modellen
    private List<String> JwsDetailValues = new ArrayList<>(Arrays.asList("org", "fn", "sub", "exp", "ean", "aud", "ref", "rnd", "rol"));
    private List<Model<String>> JwsDetailModels = new ArrayList<>(Arrays.asList(new Model<>(""), new Model<>(""), new Model<>(""), new Model<>(""), new Model<>(""), new Model<>(""), new Model<>(""), new Model<>(""), new Model<>("")));

    public HomePage(final PageParameters parameters) {
        super(parameters);

        // validatie
        add(validationBox.add(new Label("validationResult", validationResultModel)));
        add(responseInfoText);

        // token informatie
        ListView<String> lacList = initializeJwsDetails();
        add(lacList);

        // licensie
        ListView<String> licenseList = initializeTlinkDetails();
        add(licenseList);

        add(new Label("startDate", this.startDateModel));
        add(new Label("endDate", this.endDateModel));

        add(new Link<String>("licenseLink", licenseLinkModel) {
            @Override
            public void onClick() {
                createLicenses();
                setTLinks(licenseList);
                setTLinkDate();
            }
        });


        // formulier
        Form<String> tokenForm = initializeTokenForm(lacList, licenseList);

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
    private Form<String> initializeTokenForm(ListView<String> lacList, ListView<String> licenseList) {
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
                     * 6. & 7. Toon de licensies en zet het start en eind datum van de licensies
                     */
                    String jsonJws = JwsHelper.validateJws(token, "Naam van uitgever");
                    JwsHelper.createJwsPayload(jsonJws);
                    tokenIsValid(true);
                    validationResultModel.setObject("Token is valid");
                    setJwsDetails(JwsHelper.getJwsPayload(), JwsDetailModels, lacList);
                    setTLinks(licenseList);
                    setTLinkDate();

                } catch (JwsConversionException | JsonParseException | JsonMappingException re) {
                    /*
                     * 3. reset alle waarden aan de front end
                     */
                    tokenIsValid(false);
                    validationResultModel.setObject(re.getMessage());
                    resetJwsDetails(lacList);
                    resetTLinks(licenseList);
                    resetTLinkDate();

                } catch (IOException io) {
                    tokenIsValid(false);
                    validationResultModel.setObject(io.getMessage());
                    resetJwsDetails(lacList);
                    resetTLinks(licenseList);
                    resetTLinkDate();
                } finally {
                    /*
                     * 1. Stuurt de JwsPayload instantie en de JWS token naar de toegang.org/callback endpoint
                     * 2. De response status krijgt een kleur op basis van de status
                     * 3. Zet de response status in het model, zodat het aan de voorkant getoond wordt
                     */
                    Response response = ToegangClient.getInstance().sendPayloadAndToken(JwsHelper.getJwsPayload(), token);
                    setResponseColor(response.getStatus());
                    responseTextModel.setObject("" + response.getStatus());
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
                    tag.put("style", "background-color: green; " +
                            "padding: 2px; " +
                            "box-shadow: 1px 2px 6px 0px;" +
                            "border: 2px solid #007200;");
                } else {
                    tag.put("style", "background-color: red; " +
                            "padding: 2px; " +
                            "box-shadow: 1px 2px 6px 0px;" +
                            "border: 2px solid  #eb0000;");
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
     * Maakt de licensies aan en geeft deze terug. Hij slaat de licensies ook op in de LicenseCreator onder de field: 'LicenseResponse'
     *
     * @return LicenseResponse met daarin alle licensies met start en vervaldatum
     */
    private LicenseResponse createLicenses() {
        try {
            LicenseCreator.getInstance().setLicenseBodyRequest("9789492725004", "47646840-c9a2-4a5c-8430-e5377ce23210",
                    12, UUID.randomUUID().toString());
            LicenseCreator.getInstance().requestLicenses("6872181b-b4f5-4e92-9574-f8ae058485fa", "735cec20-50de-49ed-828d-f269c9cdf89a");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return LicenseCreator.getInstance().getLicenseResponse();
    }

    /**
     * Een lijst voor de licensies wordt geïnstantieerd met de juiste intellingen
     *
     * @return ListView dat gebruikt wordt om de licensies in te plaatsen
     */
    private ListView<String> initializeTlinkDetails() {
        return new ListView<String>("licenseList", LicenseCreator.getInstance().getLicenseResponse().getCodes()) {
            @Override
            protected void populateItem(ListItem<String> item) {
                item.add(new Label("license", item.getDefaultModelObjectAsString()));
            }
        };
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
     * @param models  hier zet die alle informatie in
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

    /**
     * Cropt het datum dat wordt gelezen uit de LicenseResponse en plaatst deze in zijn bijbehorende model
     */
    private void setTLinkDate(){
        startDateModel.setObject(LicenseCreator.getInstance().getLicenseResponse().getStartDate().toString().substring(0,10));
        endDateModel.setObject(LicenseCreator.getInstance().getLicenseResponse().getEndDate().toString().substring(0,10));
    }

    private void resetTLinkDate(){
        startDateModel.setObject("");
        endDateModel.setObject("");
    }

    /**
     * Vult de lijst met alle licensies, zodat ze aan de voorkant te zien zijn
     */
    private void setTLinks(ListView<String> licenseList) {
        licenseList.setList(LicenseCreator.getInstance().getLicenseResponse().getCodes());
    }

    private void resetTLinks(ListView<String> licenseList) {
        licenseList.setList(new ArrayList<>());
    }


    private void resetJwsDetails(ListView<String> lacList) {
        for (int i = 0; i < this.JwsDetailModels.size(); i++) {
            this.JwsDetailModels.get(i).setObject("");
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
