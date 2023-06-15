/**
 * Seed
 * Copyright (C) 2021 EUU⛰ROCKS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.seed.core.entity.transfer;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;

import org.seed.C;
import org.seed.core.application.AbstractRestController;
import org.seed.core.config.OpenSessionInViewFilter;
import org.seed.core.data.ValidationException;
import org.seed.core.user.Authorisation;
import org.seed.core.util.MiscUtils;
import org.seed.core.util.NameUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/seed/rest/transfer")
public class TransferRestController extends AbstractRestController<Transfer> {
	
	@Autowired
	private TransferService transferService;
	
	@Override
	protected TransferService getService() {
		return transferService;
	}
	
	@Override
	@ApiOperation(value = "getAllTransfers", notes="returns a list of all authorized transfers (data import/export)")
	@GetMapping
	public List<Transfer> getAll(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session) {
		return getAll(session, transfer -> checkPermissions(session, transfer));
	}
	
	@Override
	@ApiOperation(value = "getTransferById", notes="returns the transfer (data import/export) with the given id")
	@GetMapping(value = "/{id}")
	public Transfer get(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session,
						@PathVariable(C.ID) Long id) {
		final Transfer transfer = super.get(session, id);
		if (transfer != null && !checkPermissions(session, transfer)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		return transfer;
	}
	
	@ApiOperation(value = "exportObjects", notes = "exports and downloads the result of a transfer with the given id")
	@GetMapping(value = "/{id}/export")
	public ResponseEntity<ByteArrayResource> exportObjects(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session,
														   @PathVariable(C.ID) Long id) {
		if (!isAuthorised(session, Authorisation.RUN_IMPORT_EXPORT)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		
		final Transfer transfer = super.get(session, id);
		if (transfer == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, C.TRANSFER + ' ' + id);
		}
		else if (!checkPermissions(session, transfer)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		final String fileName = transfer.getName() + '_' + MiscUtils.getTimestampString() +
								transfer.getFormat().fileExtension;
		return download(fileName, transferService.doExport(transfer));
	}
	
	@ApiOperation(value = "importObjects", notes = "uploads and imports a file via the transfer with the given id")
	@PostMapping(value = "/{id}/import")
	public ResponseEntity<String> importObjects(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session,
												@PathVariable(C.ID) Long id,
												@RequestParam("file") MultipartFile file,
												@RequestParam("allOrNothing") Optional<String> allOrNothing,
												@RequestParam("createIfNew") Optional<String> createIfNew,
												@RequestParam("modifyExisting") Optional<String> modifyExisting,
												@RequestParam("executeCallbacks") Optional<String> executeCallbacks) {
		if (!isAuthorised(session, Authorisation.RUN_IMPORT_EXPORT)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		
		final Transfer transfer = super.get(session, id);
		if (transfer == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, C.TRANSFER + ' ' + id);
		}
		else if (!checkPermissions(session, transfer, TransferAccess.IMPORT)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		if (file == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file not found");
		}
		
		final ImportOptions options = new ImportOptions();
		options.setAllOrNothing(allOrNothing.isEmpty() || NameUtils.booleanValue(allOrNothing.get()));
		options.setCreateIfNew(createIfNew.isEmpty() || NameUtils.booleanValue(createIfNew.get()));
		options.setModifyExisting(modifyExisting.isEmpty() || NameUtils.booleanValue(modifyExisting.get()));
		options.setExecuteCallbacks(executeCallbacks.isEmpty() || NameUtils.booleanValue(executeCallbacks.get()));
		try {
			final TransferResult result = transferService.doImport(transfer, options, toFileObject(file));
			final String infoMsg = "Parameters[" + options + "], Result[" + result + ']';
			if (options.isAllOrNothing() && result.hasErrors()) {
				return new ResponseEntity<>("import failed. " + infoMsg, HttpStatus.NOT_ACCEPTABLE);
			}
			return new ResponseEntity<>(file.getOriginalFilename() + " imported. " + infoMsg, HttpStatus.OK);
		}
		catch (ValidationException vex) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, vex.getMessage());
		}
		catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

}
