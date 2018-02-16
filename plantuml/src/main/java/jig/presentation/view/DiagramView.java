package jig.presentation.view;

import jig.domain.model.Diagram;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;


public class DiagramView implements View {

    private final Diagram diagram;

    public DiagramView(Diagram diagram) {
        this.diagram = diagram;
    }

    @Override
    public String getContentType() {
        return MediaType.IMAGE_PNG.toString();
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        byte[] bytes = diagram.getBytes();
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
    }
}
