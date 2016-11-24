/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.jtrac.util;

import info.jtrac.domain.ItemUser;
import info.jtrac.domain.Space;
import info.jtrac.domain.User;
import info.jtrac.domain.UserSpaceRole;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * routines to filter User, UserSpaceRoles collections etc
 */
public class UserUtils {

    /**
     * This is a rather 'deep' concept, first of course you need to restrict the next possible
     * states that an item can be switched to based on the current state and the workflow defined.
     * But what about who all it can be assigned to?  This will be the set of users who fall into roles
     * that have permissions to transition FROM the state being switched to. Ouch.
     * This is why the item_view / history update screen has to be Ajaxed so that the drop
     * down list of users has to dynamically change based on the TO state
     */
    public static List<User> filterUsersAbleToTransitionFrom(List<UserSpaceRole> userSpaceRoles, Space space, int state) {
        Set<String> set = space.getMetadata().getRolesAbleToTransitionFrom(state);
        List<User> list = new ArrayList<User>(userSpaceRoles.size());
        for (UserSpaceRole usr : userSpaceRoles) {
            if (set.contains(usr.getRoleKey())) {
                list.add(usr.getUser());
            }
        }
        return list;
    }

    /**
     * used to init backing form object in wicket corresponding to ItemUser / notifyList
     */
    public static List<ItemUser> convertToItemUserList(List<UserSpaceRole> userSpaceRoles) {
        List<ItemUser> itemUsers = new ArrayList<ItemUser>(userSpaceRoles.size());
        Set<User> users = new HashSet<User>(itemUsers.size());
        for (UserSpaceRole usr : userSpaceRoles) {
            User user = usr.getUser();
            // we need to do this check as now JTrac supports same user mapped
            // more than once to a space with different roles
            if (!users.contains(user)) {
                users.add(user);
                itemUsers.add(new ItemUser(user));
            }
        }
        return itemUsers;
    }

    /**
     * used to prepare drop down lists for the search screen in the ui
     */
    public static Map<Long, String> getSpaceNamesMap(User user) {
        Map<Long, String> map = new HashMap<Long, String>(user.getUserSpaceRoles().size());
        for (UserSpaceRole usr : user.getUserSpaceRoles()) {
            if (usr.getSpace() != null) {
                map.put(usr.getSpace().getId(), usr.getSpace().getName());
            }
        }
        return map;
    }
}
