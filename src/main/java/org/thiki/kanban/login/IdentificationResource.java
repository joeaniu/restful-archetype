package org.thiki.kanban.login;

import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;
import org.thiki.kanban.board.BoardsController;
import org.thiki.kanban.foundation.common.RestResource;
import org.thiki.kanban.foundation.hateoas.TLink;
import org.thiki.kanban.notification.NotificationController;
import org.thiki.kanban.teams.team.TeamsController;
import org.thiki.kanban.user.UsersController;

import javax.annotation.Resource;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * Created by xubt on 7/8/16.
 */
@Service
public class IdentificationResource extends RestResource {
    @Resource
    private TLink tlink;

    public Object toResource(Identification identification) throws Exception {
        IdentificationResource identificationResource = new IdentificationResource();
        identificationResource.domainObject = identification;
        Link boardsLink = linkTo(methodOn(BoardsController.class).loadByUserName(identification.getUserName())).withRel("boards");
        identificationResource.add(tlink.from(boardsLink).build());

        Link teamsLink = linkTo(methodOn(TeamsController.class).findByUserName(identification.getUserName())).withRel("teams");
        identificationResource.add(tlink.from(teamsLink).build());

        Link notificationsLink = linkTo(methodOn(NotificationController.class).loadUnreadNotificationsTotal(identification.getUserName())).withRel("unreadNotificationsTotal");
        identificationResource.add(tlink.from(notificationsLink).build());

        Link profileLink = linkTo(methodOn(UsersController.class).loadProfile(identification.getUserName())).withRel("profile");
        identificationResource.add(tlink.from(profileLink).build());
        return identificationResource.getResource();
    }
}
