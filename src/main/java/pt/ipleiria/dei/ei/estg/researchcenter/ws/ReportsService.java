package pt.ipleiria.dei.ei.estg.researchcenter.ws;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import pt.ipleiria.dei.ei.estg.researchcenter.dtos.ActivityLogDTO;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.ActivityLogBean;
import pt.ipleiria.dei.ei.estg.researchcenter.security.Authenticated;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Path("reports")
@Authenticated
public class ReportsService {

    @EJB
    private ActivityLogBean activityLogBean;

    /**
     * EP48 - Export activity report
     * GET /api/reports/activity?userIds=1,2&dateFrom=...&dateTo=...&format=csv|json
     */
    @GET
    @Path("/activity")
    @RolesAllowed({"ADMINISTRADOR"})
    @Produces({MediaType.APPLICATION_JSON, "text/csv", "application/pdf"})
    public Response activity(@QueryParam("userIds") String userIds,
                             @QueryParam("dateFrom") String dateFromStr,
                             @QueryParam("dateTo") String dateToStr,
                             @QueryParam("format") @DefaultValue("csv") String format) {
        LocalDateTime dateFrom = null;
        LocalDateTime dateTo = null;
        try {
            if (dateFromStr != null && !dateFromStr.isBlank()) {
                dateFrom = OffsetDateTime.parse(dateFromStr).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
            }
            if (dateToStr != null && !dateToStr.isBlank()) {
                dateTo = OffsetDateTime.parse(dateToStr).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
            }
        } catch (Exception ignore) {}

        List<Long> ids = null;
        if (userIds != null && !userIds.isBlank()) {
            ids = List.of(userIds.split(",")).stream()
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .map(Long::parseLong)
                    .toList();
        }

        var logs = activityLogBean.findByFilters(ids, dateFrom, dateTo);
        var dtos = ActivityLogDTO.from(logs);

        if ("json".equalsIgnoreCase(format)) {
            return Response.ok(dtos).build();
        }

        if ("pdf".equalsIgnoreCase(format)) {
            byte[] pdf = generatePdf(dtos);
            return Response.ok(pdf, "application/pdf")
                    .header("Content-Disposition", "attachment; filename=\"activity-report.pdf\"")
                    .build();
        }

        StreamingOutput out = output -> {
            output.write("id,userId,action,entityType,entityId,description,timestamp\r\n".getBytes(StandardCharsets.UTF_8));
            for (var d : dtos) {
                String line = String.format("%d,%d,%s,%s,%d,%s,%s\r\n",
                        d.getId() != null ? d.getId() : 0,
                        d.getUserId() != null ? d.getUserId() : 0,
                        escapeCsv(d.getAction()),
                        escapeCsv(d.getEntityType()),
                        d.getEntityId() != null ? d.getEntityId() : 0,
                        escapeCsv(d.getDescription()),
                        escapeCsv(d.getTimestamp() != null ? d.getTimestamp().toString() : "")
                );
                output.write(line.getBytes(StandardCharsets.UTF_8));
            }
        };

        return Response.ok(out, "text/csv")
                .header("Content-Disposition", "attachment; filename=\"activity-report.csv\"")
                .build();
    }

    private static String escapeCsv(String v) {
        if (v == null) return "";
        String s = v.replace("\"", "\"\"");
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s + "\"";
        }
        return s;
    }

    private static byte[] generatePdf(List<ActivityLogDTO> dtos) {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
                cs.newLineAtOffset(50, 750);
                cs.showText("Activity Report");
                cs.endText();

                float y = 720;
                cs.setFont(PDType1Font.HELVETICA, 9);
                for (ActivityLogDTO d : dtos) {
                    if (y < 60) {
                        cs.close();
                        page = new PDPage();
                        doc.addPage(page);
                        y = 750;
                    }
                    String line = String.format("id=%s userId=%s action=%s entity=%s entityId=%s ts=%s",
                            d.getId(), d.getUserId(), safe(d.getAction()), safe(d.getEntityType()), d.getEntityId(),
                            d.getTimestamp() != null ? d.getTimestamp().toString() : "");
                    try (PDPageContentStream cs2 = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true)) {
                        cs2.beginText();
                        cs2.setFont(PDType1Font.HELVETICA, 9);
                        cs2.newLineAtOffset(50, y);
                        cs2.showText(truncate(line, 110));
                        cs2.endText();
                    }
                    y -= 14;
                }
            }
            doc.save(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    private static String safe(String s) { return s == null ? "" : s; }
    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }
}

