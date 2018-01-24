/**
 * Copyright (c) 2015-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package gr.xe.yeelight.types;

/**
 *
 * @author Kai Kreuzer - Initial contribution
 */
public enum OnOffType implements PrimitiveType, State, Command {
    ON,
    OFF;

    @Override
    public String format(String pattern) {
        return String.format(pattern, this.toString());
    }

}
