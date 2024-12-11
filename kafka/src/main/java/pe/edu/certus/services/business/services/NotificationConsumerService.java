package pe.edu.certus.services.business.services;

import org.springframework.stereotype.Service;
import pe.edu.certus.services.business.model.NotificationModel;

import java.util.logging.Logger;

@Service
public class NotificationConsumerService {
    private static final Logger LOGGER = Logger.getLogger(NotificationConsumerService.class.getName());

    @KafkaListener(topics = "request-notifications", groupId = "notification-group")
    public void processNotification(NotificationModel notification) {
        // Registrar la notificación
        LOGGER.info("Received notification: " + notification);

        // Procesamiento básico de notificaciones
        handleSpecificNotification(notification);
    }

    private void handleSpecificNotification(NotificationModel notification) {
        switch (notification.getType()) {
            case "REQUEST_STATUS_CHANGE":
                handleRequestStatusChange(notification);
                break;
            default:
                LOGGER.warning("Unhandled notification type: " + notification.getType());
        }
    }

    private void handleRequestStatusChange(NotificationModel notification) {
        LOGGER.info("Processing request status change notification: " + notification.getContent());
    }
}