package com.smartrecruit.analytics.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PdfReportWriter {
    public byte[] create(List<String> lines) {
        StringBuilder content=new StringBuilder("BT\n/F1 12 Tf\n50 790 Td\n");
        int count=0;
        for(String line:lines){
            if(count++>=38) break;
            String safe=line==null?"":line.replaceAll("[^\\x20-\\x7E]","?").replace("\\","\\\\").replace("(","\\(").replace(")","\\)");
            content.append('(').append(safe).append(") Tj\n0 -18 Td\n");
        }
        content.append("ET\n");
        byte[] stream=content.toString().getBytes(StandardCharsets.US_ASCII);
        String[] objects={
            "<< /Type /Catalog /Pages 2 0 R >>",
            "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
            "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>",
            "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
            "<< /Length "+stream.length+" >>\nstream\n"+content+"endstream"
        };
        try {
            ByteArrayOutputStream out=new ByteArrayOutputStream(); write(out,"%PDF-1.4\n%\u00e2\u00e3\u00cf\u00d3\n");
            int[] offsets=new int[objects.length+1];
            for(int i=0;i<objects.length;i++){
                offsets[i+1]=out.size(); write(out,(i+1)+" 0 obj\n");
                if(i==4){
                    write(out,"<< /Length "+stream.length+" >>\nstream\n"); out.write(stream); write(out,"endstream\n");
                }else write(out,objects[i]+"\n");
                write(out,"endobj\n");
            }
            int xref=out.size(); write(out,"xref\n0 "+(objects.length+1)+"\n0000000000 65535 f \n");
            for(int i=1;i<offsets.length;i++) write(out,String.format(java.util.Locale.ROOT,"%010d 00000 n \n",offsets[i]));
            write(out,"trailer\n<< /Size "+(objects.length+1)+" /Root 1 0 R >>\nstartxref\n"+xref+"\n%%EOF\n");
            return out.toByteArray();
        } catch(java.io.IOException impossible){throw new IllegalStateException("PDF report could not be generated",impossible);}
    }
    private static void write(ByteArrayOutputStream out,String value)throws java.io.IOException {out.write(value.getBytes(StandardCharsets.US_ASCII));}
}
