package pe.edu.certus.services.business.adapter.drivers;

import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import pe.edu.certus.services.business.domain.RequestModel;
import pe.edu.certus.services.business.ports.drivers.ForRequest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/requests")
public class RequestAdapter {

    private final ForRequest forRequest;

    public RequestAdapter(ForRequest forRequest) {
        this.forRequest = forRequest;
    }

    @PostMapping("/create")
    public ResponseEntity<RequestWebModel> createRequest(@RequestBody RequestWebModel requestWebModel) {
        try {
            if (requestWebModel.category() == null || requestWebModel.description() == null) {
                return ResponseEntity.badRequest().build();
            }

            RequestModel request = RequestWebModel.toDomainModel(requestWebModel);
            request.setStatus(request.getStatus() != null ? request.getStatus() : false);

            forRequest.createRequestModel(request);
            return ResponseEntity.ok(RequestWebModel.fromDomainModel(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/batch")
    public ResponseEntity<List<RequestWebModel>> createRequests(@RequestBody List<RequestWebModel> requestWebModels) {
        try {
            List<RequestWebModel> createdRequests = new ArrayList<>();

            for (RequestWebModel requestWebModel : requestWebModels) {
                if (requestWebModel.category() == null ||
                        requestWebModel.description() == null) {
                    continue;
                }

                RequestModel request = RequestWebModel.toDomainModel(requestWebModel);
                request.setStatus(request.getStatus() != null ? request.getStatus() : false);

                forRequest.createRequestModel(request);
                createdRequests.add(RequestWebModel.fromDomainModel(request));
            }

            if (createdRequests.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            return ResponseEntity.ok(createdRequests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/query")
    public List<RequestWebModel> getAllRequests() {
        List<RequestModel> requests = forRequest.findAllRequestModel();
        return requests.stream()
                .map(RequestWebModel::fromDomainModel)
                .toList();
    }

    @GetMapping("/find/{id}")
    public ResponseEntity<RequestWebModel> getRequestById(@PathVariable Long id) {
        RequestModel request = (RequestModel) forRequest.findRequestModelById(id);
        if (request == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(RequestWebModel.fromDomainModel(request));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteRequestBy(@PathVariable Long id) {
        RequestModel request = (RequestModel) forRequest.findRequestModelById(id);
        if (request == null) {
            return ResponseEntity.notFound().build();
        }
        forRequest.deleteRequestModelById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<RequestWebModel> updateRequest(@PathVariable Long id, @RequestBody RequestWebModel requestWebModel) {
        RequestModel existingRequest = (RequestModel) forRequest.findRequestModelById(id);
        if (existingRequest == null) {
            return ResponseEntity.notFound().build();
        }

        RequestModel requestToUpdate = RequestWebModel.toDomainModel(requestWebModel);

        requestToUpdate.setId(id);

        RequestModel updatedRequest = (RequestModel) forRequest.updateRequestModel(requestToUpdate);

        return ResponseEntity.ok(RequestWebModel.fromDomainModel(updatedRequest));
    }

    @GetMapping("/editar/{id}")
    public ModelAndView showEditRequestPage(@PathVariable Long id) {
        RequestModel request = (RequestModel) forRequest.findRequestModelById(id);
        if (request == null) {
            return new ModelAndView("redirect:/");
        }
        
        ModelAndView modelAndView = new ModelAndView("edit-request");
        modelAndView.addObject("request", request);
        return modelAndView;
    }

    @PostMapping("/editar/{id}")
    public ModelAndView editRequest(@PathVariable Long id, @ModelAttribute RequestWebModel requestWebModel) {
        RequestModel existingRequest = (RequestModel) forRequest.findRequestModelById(id);
        if (existingRequest == null) {
            return new ModelAndView("redirect:/");
        }

        RequestModel requestToUpdate = RequestWebModel.toDomainModel(requestWebModel);
        requestToUpdate.setId(id);

        boolean statusChanged = existingRequest.getStatus() != requestToUpdate.getStatus();

        forRequest.updateRequestModel(requestToUpdate);

        ModelAndView modelAndView = new ModelAndView("redirect:/requests");

        if (statusChanged) {
            String notificationScript = String.format(
                    "<script>window.showNotification({ " +
                            "requestId: %d, " +
                            "requestStatus: %b, " +
                            "content: 'Solicitud de Viáticos #%d ha sido %s' " +
                            "});</script>",
                    requestToUpdate.getId(),
                    requestToUpdate.getStatus(),
                    requestToUpdate.getId(),
                    requestToUpdate.getStatus() ? "APROBADA" : "RECHAZADA"
            );
            modelAndView.addObject("notificationScript", notificationScript);
        }

        return modelAndView;
    }

    @GetMapping("/eliminar/{id}")
    public ModelAndView deleteRequest(@PathVariable Long id) {
        RequestModel request = (RequestModel) forRequest.findRequestModelById(id);
        if (request != null) {
            forRequest.deleteRequestModelById(id);
        }
        return new ModelAndView("redirect:/requests");
    }

    @GetMapping
    public ModelAndView listRequests() {
        List<RequestModel> requests = forRequest.findAllRequestModel();
        
        ModelAndView modelAndView = new ModelAndView("viaticos");
        modelAndView.addObject("requests", requests);
        return modelAndView;
    }

    @GetMapping("/crear")
    public String showCreateRequestPage(Model model) {
        model.addAttribute("request", RequestWebModel.builder()
            .category("")
            .description("")
            .status(false)
            .build());
        return "crear-request";
    }

    @PostMapping("/crear")
    public ModelAndView createRequestWeb(@ModelAttribute RequestWebModel requestWebModel) {
        RequestModel newRequest = RequestWebModel.toDomainModel(requestWebModel);
        
        // Set default status to false (PENDIENTE)
        newRequest.setStatus(false);
        
        forRequest.createRequestModel(newRequest);
        
        return new ModelAndView("redirect:/requests");
    }
}
