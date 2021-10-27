package uk.nhs.digital.docstore;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;

@SuppressWarnings("unused")
public class DocumentUploadedEventHandler implements RequestHandler<S3Event, Void>  {
    @Override
    public Void handleRequest(S3Event input, Context context) {
        System.out.println("Upload event occurred");
        return null;
    }
}
