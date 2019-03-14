package nl.topicus.example;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.topicus.example.client.ToegangOrg;
import nl.topicus.example.exception.JwsConversionException;
import nl.topicus.example.model.JwsPayload;
import nl.topicus.example.security.constants;
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
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.ErrorCodes;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class HomePage extends WebPage {

    private static final long serialVersionUID = 1L;

    private long JwsExpInMs = 0;
    private JwsPayload payload = new JwsPayload();

    private Model<String> inputModel = new Model<>("");
    private Model<String> responseTextModel = new Model<>("");
    private Model<String> validationResultModel = new Model<>("");
    private Model<String> validationResultBoxModel = new Model<>("");

    private WebMarkupContainer validationBox = new WebMarkupContainer("validationResultBox", validationResultBoxModel);
    private Label responseInfoText = new Label("responseInfoText", responseTextModel);

    private List<String> values = new ArrayList<>(Arrays.asList("org", "fn", "sub", "exp", "tlink", "ean", "aud", "ref", "rnd", "rol"));
    private List<Model<String>> models = new ArrayList<>(Arrays.asList(new Model<>(""), new Model<>(""), new Model<>(""), new Model<>(""), new Model<>(""), new Model<>(""), new Model<>(""), new Model<>(""), new Model<>(""), new Model<>("")));


    public HomePage(final PageParameters parameters) {
        super(parameters);

        add(validationBox.add(new Label("validationResult", validationResultModel)));
        add(responseInfoText);

        ListView<String> lacList = initializeJwsDetails();
        add(lacList);

        // json object wordt gemaakt voor de request
//        try {
//            System.out.println(new ObjectMapper().writeValueAsString(new LicenseRequestModel("application/json", "Bearer", "This is the token", new LicenseModel("productId", "distributorId", 200, "requestReferenceId"))));
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

    private Form<String> initializeTokenForm(ListView<String> lacList) {
        return new Form<String>("tokenForm") {

            @Override
            protected void onSubmit() {
                String token = inputModel.getObject();
                try {
                    String jsonJws = HomePage.this.validateJws(token, "Naam van uitgever");
                    payload = new ObjectMapper().readValue(jsonJws, JwsPayload.class);
                    setValidBoxColor(true);
                    validationResultModel.setObject("Token is valid");
                    setJwsDetails(payload, models, lacList);

                } catch (JwsConversionException | JsonParseException | JsonMappingException re) {
                    setValidBoxColor(false);
                    validationResultModel.setObject(re.getMessage());
                    resetJwsDetails(payload, models, lacList);

                } catch (IOException io) {
                    setValidBoxColor(false);
                    validationResultModel.setObject(io.getMessage());
                    resetJwsDetails(payload, models, lacList);
                } finally {
                    Response response = ToegangOrg.sendPayload(payload, token);
                    setResponseColor(response.getStatus());
                    responseTextModel.setObject(""+response.getStatus());
                }
            }
        };
    }

    private void setValidBoxColor(boolean valid) {
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

    private ListView<String> initializeJwsDetails() {

        ListView<String> lacList = new ListView<String>("lacList", this.payload.getLac()) {
            @Override
            protected void populateItem(ListItem<String> item) {
                item.add(new Label("lacItem", item.getDefaultModelObjectAsString()));
            }
        };

        for (int i = 0; i < this.values.size(); i++) {
            add(new Label(this.values.get(i), this.models.get(i)));
        }

        return lacList;
    }

    private void setJwsDetails(JwsPayload payload, List<Model<String>> models, ListView<String> lacList) {
        for (int i = 0; i < payload.getValues().size(); i++) {
            if (i == 3) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(this.JwsExpInMs);
                models.get(i).setObject("" + c.getTime());
            } else {
                models.get(i).setObject(payload.getValues().get(i));
            }
        }

        lacList.setList(payload.getLac());
    }

    private void resetJwsDetails(JwsPayload payload, List<Model<String>> models, ListView<String> lacList) {
        for (int i = 0; i < payload.getValues().size(); i++) {
            models.get(i).setObject(" ");
        }

        lacList.setList(new ArrayList<>());
    }


    private String validateJws(String token, String publisherName) throws JwsConversionException {
        try {
            byte[] data = Base64.getDecoder().decode((constants.PUBLIC_KEY.getBytes()));
            X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
            KeyFactory fact = KeyFactory.getInstance("RSA");
            PublicKey key = fact.generatePublic(spec);

            JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                    .setAllowedClockSkewInSeconds(30)
                    .setRequireSubject()
                    .setExpectedAudience(publisherName)
                    .setVerificationKey(key)
                    .setJwsAlgorithmConstraints(
                            new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST,
                                    AlgorithmIdentifiers.RSA_USING_SHA256))
                    .build();

            JwtClaims jwtClaims = jwtConsumer.processToClaims(token);

            extractExpToMs(jwtClaims);

            if (NumericDate.now().isOnOrAfter(NumericDate.fromMilliseconds(this.JwsExpInMs))) {
                throw new JwsConversionException("JWS token has expired");
            }

            return jwtClaims.toJson();

        } catch (InvalidJwtException e) {
            if (e.hasErrorCode(ErrorCodes.AUDIENCE_INVALID)) {
                throw new JwsConversionException("JWS token contains the wrong audience");
            }

        } catch (MalformedClaimException | NoSuchAlgorithmException | InvalidKeySpecException multiE) {
            multiE.printStackTrace();
        }

        throw new JwsConversionException("Invalid token value");
    }

    private void extractExpToMs(JwtClaims jwtClaims) throws MalformedClaimException {
        this.JwsExpInMs = jwtClaims.getExpirationTime().getValueInMillis() / 1000;
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        PackageResourceReference cssFile =
                new PackageResourceReference(this.getClass(), "homepage.css");
        CssHeaderItem cssItem = CssHeaderItem.forReference(cssFile);

        response.render(cssItem);
    }
}
